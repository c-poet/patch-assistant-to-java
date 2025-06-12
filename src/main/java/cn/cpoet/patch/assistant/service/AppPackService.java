package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.DockerConf;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.ProgressContext;
import cn.cpoet.patch.assistant.view.tree.*;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 应用包处理
 *
 * @author CPoet
 */
public class AppPackService extends BasePackService {

    public static AppPackService getInstance() {
        return AppContext.getInstance().getService(AppPackService.class);
    }

    /**
     * 获取树形节点
     *
     * @param file 文件
     * @return 树形信息
     */
    public TreeInfo getTreeNode(File file) {
        TreeInfo treeInfo = new TreeInfo();
        FileNode rootNode = new FileNode();
        rootNode.setName(file.getName());
        rootNode.setText(file.getName());
        rootNode.setPath(file.getPath());
        rootNode.setFile(file);
        treeInfo.setRootNode(rootNode);
        try (InputStream in = new FileInputStream(file);
             ZipInputStream zin = new ZipInputStream(in)) {
            doReadZipEntry(rootNode, zin, false);
        } catch (IOException ex) {
            throw new AppException("读取文件失败", ex);
        }
        return treeInfo;
    }

    protected void writePatchMeta(ZipOutputStream zipOut, TreeInfo patchTree) throws IOException {
        boolean isWritePathMeta = Boolean.TRUE.equals(Configuration.getInstance().getPatch().getWritePatchMeta());
        if (!isWritePathMeta) {
            return;
        }
        ZipEntry entry = new ZipEntry("PATCH-INF/" + DateUtil.formatDateTime(LocalDateTime.now()));
        entry.setTimeLocal(LocalDateTime.now());
        entry.setComment("Patch meta file");
        zipOut.putNextEntry(entry);
        zipOut.write(("Name: " + patchTree.getRootNode().getName()).getBytes());
    }

    /**
     * 生成并保存应用包
     *
     * @param context       进度上下文
     * @param file          文件名
     * @param appTree       应用树
     * @param patchNode     补丁树
     * @param isDockerImage 是否Docker镜像
     */
    public void savePack(ProgressContext context, File file, TreeInfo appTree, TreeInfo patchNode, boolean isDockerImage) {
        if (isDockerImage) {
            String dockerfile = FileUtil.readFileAsString(AppConst.DOCKERFILE_FILE_NAME);
            if (StringUtil.isBlank(dockerfile)) {
                throw new AppException("Dockerfile模板文件不能为空");
            }
            // 校验远程信息是否配置正确
            Thread thread = new Thread(() -> {
                context.setRunLater(true);
                context.step("开始保存镜像包");
                try {
                    saveDockerPack(context, file, appTree, patchNode, dockerfile);
                    context.step("完成保存镜像包");
                } catch (Exception e) {
                    context.step(ExceptionUtil.asString(e));
                } finally {
                    context.end();
                }
            });
            thread.setDaemon(true);
            thread.start();
            return;
        }
        Thread thread = new Thread(() -> {
            context.setRunLater(true);
            context.step("开始保存应用包");
            try {
                savePack(context, file, appTree, patchNode);
                context.step("完成保存应用包");
            } catch (Exception e) {
                context.step(ExceptionUtil.asString(e));
            } finally {
                context.end();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 保存Docker镜像包
     *
     * @param context    进度上下文
     * @param file       文件
     * @param appTree    应用树
     * @param dockerfile dockerfile文件内容
     */
    public void saveDockerPack(ProgressContext context, File file, TreeInfo appTree, TreeInfo patchNode, String dockerfile) {
        byte[] bytes;
        TreeNode rootNode = appTree.getRootNode();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            writePatchMeta(zipOut, patchNode);
            doSavePack(context, rootNode, zipOut);
            bytes = out.toByteArray();
        } catch (Exception e) {
            throw new AppException("生成应用包失败", e);
        }
        dockerfile = TemplateUtil.render(dockerfile, Collections.singletonMap("jarName", rootNode.getName()));
        DockerConf docker = Configuration.getInstance().getDocker();
        if (DockerConf.TYPE_REMOTE.equals(docker.getType())) {
            saveDockerPackWithRemote(context, docker, file, rootNode, bytes, dockerfile);
            return;
        }
        saveDockerPackWithLocal(context, docker, file, rootNode, bytes, dockerfile);
    }

    protected void saveDockerPackWithRemote(ProgressContext context, DockerConf docker, File file, TreeNode rootNode, byte[] bytes, String dockerfile) {
        context.step("校验Docker服务器信息");
        String host = docker.getHost();
        String username = docker.getUsername();
        String password = docker.getPassword();
        if (StringUtil.isBlank(host) || StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
            throw new AppException("Docker服务器信息错误，请检查配置");
        }
        context.step("SSH连接到: " + docker.getHost());
        Session session = null;
        try {
            password = EncryptUtil.decryptWithRsaSys(password);
            session = SSHUtil.createSession(host, docker.getPort(), username, password);
            context.step("SSH连接成功");
            OutputStream progressOut = context.createOutputStream();
            String command = StringUtil.isBlank(docker.getCommand()) ? DockerConf.DEFAULT_COMMAND : docker.getCommand();
            String workPath = StringUtil.isBlank(docker.getWorkPath()) ? DockerConf.DEFAULT_WORK_PATH : docker.getWorkPath();
            context.step("上传Dockerfile");
            SSHUtil.uploadFile(session, progressOut, createSftpProgressMonitor(context, true), workPath, AppConst.DOCKERFILE_FILE_NAME, dockerfile.getBytes());
            context.step("上传应用包: " + rootNode.getName());
            SSHUtil.uploadFile(session, progressOut, createSftpProgressMonitor(context, true), workPath, rootNode.getName(), bytes);
            context.step("生成Docker镜像");
            int status = SSHUtil.execCmd(session, progressOut, command + " build -t demo:1.0.0 " + workPath);
            if (status != 0) {
                throw new AppException("生成Docker镜像失败");
            }
            context.step("导出Docker镜像");
            status = SSHUtil.execCmd(session, progressOut, command + " save -o " + workPath + "/demo.tar demo:1.0.0");
            if (status != 0) {
                throw new AppException("导出Docker镜像失败");
            }
            context.step("下载Docker镜像");
            SSHUtil.downloadFile(session, progressOut, createSftpProgressMonitor(context, false), workPath + "/demo.tar", file);
        } finally {
            context.step("关闭SSH连接");
            SSHUtil.closeSession(session);
        }
    }

    protected void saveDockerPackWithLocal(ProgressContext context, DockerConf docker, File file, TreeNode rootNode, byte[] bytes, String dockerfile) {
        String localCommand = StringUtil.isBlank(docker.getLocalCommand()) ? DockerConf.DEFAULT_COMMAND : docker.getLocalCommand();
        String localWorkPath = StringUtil.isBlank(docker.getLocalWorkPath()) ? DockerConf.DEFAULT_WORK_PATH : docker.getLocalWorkPath();
        context.step("写入Dockerfile到: " + localWorkPath);
        FileUtil.writeFile(localWorkPath, AppConst.DOCKERFILE_FILE_NAME, dockerfile.getBytes());
        context.step("写入应用包到: " + localWorkPath);
        FileUtil.writeFile(localWorkPath, rootNode.getName(), bytes);
        context.step("生成Docker镜像");
        OutputStream progressOut = context.createOutputStream();
        int status = CommandUtil.exec(localCommand + " build -t demo:1.0.0 .", localWorkPath, progressOut);
        if (status != 0) {
            throw new AppException("生成Docker镜像失败");
        }
        context.step("导出Docker镜像");
        status = CommandUtil.exec(localCommand + " save -o demo.tar demo:1.0.0", localWorkPath, progressOut);
        if (status != 0) {
            throw new AppException("导出Docker镜像失败");
        }
        context.step("移动Docker镜像");
        String sourcePath = FileNameUtil.joinPath(localWorkPath, "demo.tar");
        FileUtil.moveFile(sourcePath, file, StandardCopyOption.REPLACE_EXISTING);
    }

    protected SftpProgressMonitor createSftpProgressMonitor(ProgressContext context, boolean isUpload) {
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
                context.step(message);
            }

            @Override
            public boolean count(long count) {
                if (opCount == 0) {
                    context.step((isUpload ? "已上传 " : "已下载 ") + FileUtil.getSizeReadability(opCount += count));
                } else {
                    context.overwrite((isUpload ? "已上传 " : "已下载 ") + FileUtil.getSizeReadability(opCount += count));
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
                context.step(message + " 结束");
            }
        };
    }

    /**
     * 生成并保存应用包
     *
     * @param file    文件
     * @param appTree 应用树
     */
    public void savePack(ProgressContext context, File file, TreeInfo appTree, TreeInfo patchNode) {
        TreeNode rootNode = appTree.getRootNode();
        try (OutputStream out = new FileOutputStream(file);
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            writePatchMeta(zipOut, patchNode);
            doSavePack(context, rootNode, zipOut);
        } catch (Exception e) {
            throw new AppException("生成应用包失败", e);
        }
    }

    protected void doSavePack(ProgressContext context, TreeNode rootNode, ZipOutputStream zipOut) throws IOException {
        context.step("开始写入应用包");
        if (rootNode.getChildren() != null) {
            context.step("应用包名称: " + rootNode.getName());
            for (TreeNode child : rootNode.getChildren()) {
                writeTreeNode2Pack(context, zipOut, (ZipEntryNode) child);
            }
        }
        zipOut.finish();
        context.step("完成应用包写入");
    }

    protected void writeTreeNode2Pack(ProgressContext context, ZipOutputStream zipOut, ZipEntryNode node) throws IOException {
        // 标记为删除状态的节点不在写入新的包中
        TreeNodeStatus status = node.getStatus();
        if (TreeNodeStatus.DEL.equals(status) || TreeNodeStatus.MARK_DEL.equals(status)) {
            context.step("删除文件: " + node.getName());
            return;
        }
        if (!node.isDir() && node.getText().endsWith(FileExtConst.DOT_JAR)) {
            context.step("写入: " + node.getName());
            writeTreeNode2PackWithJar(context, zipOut, node);
            return;
        }
        if (node.getMappedNode() == null) {
            zipOut.putNextEntry(getNewEntryWithZipEntry(node.getEntry()));
            if (!node.isDir()) {
                context.step("写入: " + node.getName());
                zipOut.write(node.getBytes());
            }
        } else {
            TreeNode mappedNode = node.getMappedNode();
            ZipEntry zipEntry = getNewEntryWithZipEntry(node.getEntry());
            zipEntry.setTimeLocal(mappedNode.getModifyTime());
            zipOut.putNextEntry(zipEntry);
            if (!zipEntry.isDirectory()) {
                context.step("写入: " + node.getName());
                zipOut.write(mappedNode.getBytes());
            }
        }
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                writeTreeNode2Pack(context, zipOut, (ZipEntryNode) child);
            }
        }
    }

    protected void writeTreeNode2PackWithJar(ProgressContext context, ZipOutputStream zipOut, ZipEntryNode node) throws IOException {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            zipOut.putNextEntry(getNewEntryWithZipEntry(node.getEntry()));
            zipOut.write(node.getBytes());
            return;
        }
        byte[] bytes = getBytesWithJarNode(context, node);
        ZipEntry zipEntry = getNewEntryWithZipEntry(node.getEntry());
        zipEntry.setSize(bytes.length);
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        zipEntry.setCrc(crc32.getValue());
        zipEntry.setTimeLocal(LocalDateTime.now());
        zipOut.putNextEntry(zipEntry);
        zipOut.write(bytes);
    }

    protected byte[] getBytesWithJarNode(ProgressContext context, ZipEntryNode jarNode) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (TreeNode child : jarNode.getChildren()) {
                writeTreeNode2Pack(context, zipOut, (ZipEntryNode) child);
            }
            zipOut.finish();
            return out.toByteArray();
        }
    }

    protected ZipEntry getNewEntryWithZipEntry(ZipEntry zipEntry) {
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
