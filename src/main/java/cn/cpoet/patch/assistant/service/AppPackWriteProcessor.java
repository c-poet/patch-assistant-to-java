package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.DockerConf;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.HomeContext;
import cn.cpoet.patch.assistant.view.ProgressContext;
import cn.cpoet.patch.assistant.view.tree.AppTreeView;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.TreeNodeStatus;
import cn.cpoet.patch.assistant.view.tree.ZipEntryNode;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 打补丁处理
 *
 * @author CPoet
 */
public class AppPackWriteProcessor {

    private final HomeContext context;
    private final boolean isDockerImage;
    private final ProgressContext progressContext;

    public AppPackWriteProcessor(HomeContext context, ProgressContext progressContext, boolean isDockerImage) {
        this.context = context;
        this.progressContext = progressContext;
        this.isDockerImage = isDockerImage;
    }

    public void exec(File file) {
        if (isDockerImage) {
            String dockerfile = FileUtil.readFileAsString(AppConst.DOCKERFILE_FILE_NAME);
            if (StringUtil.isBlank(dockerfile)) {
                throw new AppException("Dockerfile模板文件不能为空");
            }
            createDockerTask(file, dockerfile).start();
            return;
        }
        createTask(file).start();
    }

    private Thread createDockerTask(File file, String dockerfile) {
        Thread thread = new Thread(() -> {
            progressContext.setRunLater(true);
            progressContext.step("开始保存镜像包");
            try {
                writeDocker(file, dockerfile);
                progressContext.step("完成保存镜像包");
            } catch (Exception e) {
                progressContext.step(ExceptionUtil.asString(e));
            } finally {
                progressContext.end();
            }
        });
        thread.setDaemon(true);
        return thread;
    }

    private Thread createTask(File file) {
        Thread thread = new Thread(() -> {
            progressContext.setRunLater(true);
            progressContext.step("开始保存应用包");
            try {
                write(file);
                progressContext.step("完成保存应用包");
            } catch (Exception e) {
                progressContext.step(ExceptionUtil.asString(e));
            } finally {
                progressContext.end();
            }
        });
        thread.setDaemon(true);
        return thread;
    }

    private void writeDocker(File file, String dockerfile) {
        byte[] bytes;
        TreeNode rootNode = context.getAppTree().getTreeInfo().getRootNode();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            doWrite(rootNode, zipOut);
            bytes = out.toByteArray();
        } catch (Exception e) {
            throw new AppException("生成应用包失败", e);
        }
        dockerfile = TemplateUtil.render(dockerfile, Collections.singletonMap("jarName", rootNode.getName()));
        DockerConf docker = Configuration.getInstance().getDocker();
        if (DockerConf.TYPE_REMOTE.equals(docker.getType())) {
            writeDockerWithRemote(docker, file, rootNode, bytes, dockerfile);
            return;
        }
        writeDockerWithLocal(docker, file, rootNode, bytes, dockerfile);
    }

    private void writeDockerWithRemote(DockerConf docker, File file, TreeNode rootNode, byte[] bytes, String dockerfile) {
        progressContext.step("校验Docker服务器信息");
        String host = docker.getHost();
        String username = docker.getUsername();
        String password = docker.getPassword();
        if (StringUtil.isBlank(host) || StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
            throw new AppException("Docker服务器信息错误，请检查配置");
        }
        progressContext.step("SSH连接到: " + docker.getHost());
        Session session = null;
        try {
            password = EncryptUtil.decryptWithRsaSys(password);
            session = SSHUtil.createSession(host, docker.getPort(), username, password);
            progressContext.step("SSH连接成功");
            OutputStream progressOut = progressContext.createOutputStream();
            String command = StringUtil.isBlank(docker.getCommand()) ? DockerConf.DEFAULT_COMMAND : docker.getCommand();
            String workPath = StringUtil.isBlank(docker.getWorkPath()) ? DockerConf.DEFAULT_WORK_PATH : docker.getWorkPath();
            progressContext.step("上传Dockerfile");
            SSHUtil.uploadFile(session, progressOut, createSftpProgressMonitor(true), workPath, AppConst.DOCKERFILE_FILE_NAME, dockerfile.getBytes());
            progressContext.step("上传应用包: " + rootNode.getName());
            SSHUtil.uploadFile(session, progressOut, createSftpProgressMonitor(true), workPath, rootNode.getName(), bytes);
            progressContext.step("生成Docker镜像");
            int status = SSHUtil.execCmd(session, progressOut, command + " build -t demo:1.0.0 " + workPath);
            if (status != 0) {
                throw new AppException("生成Docker镜像失败");
            }
            progressContext.step("导出Docker镜像");
            status = SSHUtil.execCmd(session, progressOut, command + " save -o " + workPath + "/demo.tar demo:1.0.0");
            if (status != 0) {
                throw new AppException("导出Docker镜像失败");
            }
            progressContext.step("下载Docker镜像");
            SSHUtil.downloadFile(session, progressOut, createSftpProgressMonitor(false), workPath + "/demo.tar", file);
        } finally {
            progressContext.step("关闭SSH连接");
            SSHUtil.closeSession(session);
        }
    }

    private void writeDockerWithLocal(DockerConf docker, File file, TreeNode rootNode, byte[] bytes, String dockerfile) {
        String localCommand = StringUtil.isBlank(docker.getLocalCommand()) ? DockerConf.DEFAULT_COMMAND : docker.getLocalCommand();
        String localWorkPath = StringUtil.isBlank(docker.getLocalWorkPath()) ? DockerConf.DEFAULT_WORK_PATH : docker.getLocalWorkPath();
        progressContext.step("写入Dockerfile到: " + localWorkPath);
        FileUtil.writeFile(localWorkPath, AppConst.DOCKERFILE_FILE_NAME, dockerfile.getBytes());
        progressContext.step("写入应用包到: " + localWorkPath);
        FileUtil.writeFile(localWorkPath, rootNode.getName(), bytes);
        progressContext.step("生成Docker镜像");
        OutputStream progressOut = progressContext.createOutputStream();
        int status = CommandUtil.exec(localCommand + " build -t demo:1.0.0 .", localWorkPath, progressOut);
        if (status != 0) {
            throw new AppException("生成Docker镜像失败");
        }
        progressContext.step("导出Docker镜像");
        status = CommandUtil.exec(localCommand + " save -o demo.tar demo:1.0.0", localWorkPath, progressOut);
        if (status != 0) {
            throw new AppException("导出Docker镜像失败");
        }
        progressContext.step("移动Docker镜像");
        String sourcePath = FileNameUtil.joinPath(localWorkPath, "demo.tar");
        FileUtil.moveFile(sourcePath, file, StandardCopyOption.REPLACE_EXISTING);
    }

    private SftpProgressMonitor createSftpProgressMonitor(boolean isUpload) {
        return new SftpProgressMonitor() {

            long opCount;
            private String src;
            private String desc;

            @Override
            public void init(int op, String src, String dest, long max) {
                this.opCount = 0;
                this.src = src;
                this.desc = dest;
                String message = isUpload ? "上传 " : "下载 ";
                if (!StringUtil.isBlank(src)) {
                    message += src + " -> ";
                }
                if (!StringUtil.isBlank(dest)) {
                    message += dest;
                }
                if (max > 0) {
                    message += " 大小: " + FileUtil.getSizeReadability(max);
                }
                progressContext.step(message);
            }

            @Override
            public boolean count(long count) {
                if (opCount == 0) {
                    progressContext.step((isUpload ? "已上传 " : "已下载 ") + FileUtil.getSizeReadability(opCount += count));
                } else {
                    progressContext.overwrite((isUpload ? "已上传 " : "已下载 ") + FileUtil.getSizeReadability(opCount += count));
                }
                return true;

            }

            @Override
            public void end() {
                String message = isUpload ? "上传 " : "下载 ";
                if (!StringUtil.isBlank(src)) {
                    message += src + " -> ";
                }
                if (!StringUtil.isBlank(desc)) {
                    message += desc;
                }
                progressContext.step(message + " 结束");
            }
        };
    }

    private void write(File file) {
        AppTreeView appTree = context.getAppTree();
        TreeNode rootNode = appTree.getTreeInfo().getRootNode();
        try (OutputStream out = new FileOutputStream(file);
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            doWrite(rootNode, zipOut);
        } catch (Exception e) {
            throw new AppException("生成应用包失败", e);
        }
    }

    private void doWrite(TreeNode rootNode, ZipOutputStream zipOut) throws IOException {
        progressContext.step("开始写入应用包");
        if (rootNode.getChildren() != null) {
            progressContext.step("应用包名称: " + rootNode.getName());
            for (TreeNode child : rootNode.getChildren()) {
                writeTreeNode2Pack(zipOut, (ZipEntryNode) child);
            }
        }
        zipOut.finish();
        progressContext.step("完成应用包写入");
    }

    private void writeTreeNode2Pack(ZipOutputStream zipOut, ZipEntryNode node) throws IOException {
        // 标记为删除状态的节点不在写入新的包中
        TreeNodeStatus status = node.getStatus();
        if (TreeNodeStatus.DEL.equals(status) || TreeNodeStatus.MANUAL_DEL.equals(status)) {
            progressContext.step("删除文件: " + node.getName());
            return;
        }
        if (!node.isDir() && node.getText().endsWith(FileExtConst.DOT_JAR)) {
            progressContext.step("写入: " + node.getName());
            writeTreeNode2PackWithJar(zipOut, node);
            return;
        }
        if (node.getMappedNode() == null) {
            zipOut.putNextEntry(getNewEntryWithZipEntry(node.getEntry()));
            if (!node.isDir()) {
                progressContext.step("写入: " + node.getName());
                zipOut.write(node.getBytes());
            }
        } else {
            TreeNode mappedNode = node.getMappedNode();
            ZipEntry zipEntry = getNewEntryWithZipEntry(node.getEntry());
            zipEntry.setTimeLocal(mappedNode.getModifyTime());
            zipOut.putNextEntry(zipEntry);
            if (!zipEntry.isDirectory()) {
                progressContext.step("写入: " + node.getName());
                zipOut.write(mappedNode.getBytes());
            }
        }
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                writeTreeNode2Pack(zipOut, (ZipEntryNode) child);
            }
        }
    }

    private void writeTreeNode2PackWithJar(ZipOutputStream zipOut, ZipEntryNode node) throws IOException {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            zipOut.putNextEntry(getNewEntryWithZipEntry(node.getEntry()));
            zipOut.write(node.getBytes());
            return;
        }
        byte[] bytes = getBytesWithJarNode(node);
        ZipEntry zipEntry = getNewEntryWithZipEntry(node.getEntry());
        zipEntry.setSize(bytes.length);
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        zipEntry.setCrc(crc32.getValue());
        zipEntry.setTimeLocal(LocalDateTime.now());
        zipOut.putNextEntry(zipEntry);
        zipOut.write(bytes);
    }

    private byte[] getBytesWithJarNode(ZipEntryNode jarNode) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (TreeNode child : jarNode.getChildren()) {
                writeTreeNode2Pack(zipOut, (ZipEntryNode) child);
            }
            zipOut.finish();
            return out.toByteArray();
        }
    }

    private ZipEntry getNewEntryWithZipEntry(ZipEntry zipEntry) {
        String name = zipEntry.getName();
        ZipEntry newEntry = new ZipEntry(name);
        newEntry.setComment(zipEntry.getComment());
        if (zipEntry.getCreationTime() != null) {
            newEntry.setCreationTime(zipEntry.getCreationTime());
        }
        if (zipEntry.getLastAccessTime() != null) {
            newEntry.setLastAccessTime(zipEntry.getLastAccessTime());
        }
        newEntry.setLastModifiedTime(zipEntry.getLastModifiedTime());
        newEntry.setExtra(zipEntry.getExtra());
        if (name.endsWith(FileExtConst.DOT_JAR)) {
            newEntry.setMethod(ZipEntry.STORED);
            newEntry.setSize(zipEntry.getSize());
            newEntry.setCrc(zipEntry.getCrc());
        } else {
            newEntry.setMethod(ZipEntry.DEFLATED);
        }
        return newEntry;
    }
}
