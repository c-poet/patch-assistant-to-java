package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.JarInfoConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.DockerConf;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.model.AppPackSign;
import cn.cpoet.patch.assistant.model.PatchUpSign;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.HomeContext;
import cn.cpoet.patch.assistant.view.ProgressContext;
import cn.cpoet.patch.assistant.view.tree.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
                throw new AppException("Dockerfile template file cannot be empty");
            }
            createDockerTask(file, dockerfile).start();
            return;
        }
        createTask(file).start();
    }

    private Thread createDockerTask(File file, String dockerfile) {
        Thread thread = new Thread(() -> {
            progressContext.setRunLater(true);
            progressContext.step("Start write to Docker image pack");
            try {
                writeDocker(file, dockerfile);
                progressContext.step("Write to Docker image pack finish");
                progressContext.end(true);
            } catch (Exception e) {
                progressContext.step(ExceptionUtil.asString(e));
                progressContext.end(false);
            }
        });
        thread.setDaemon(true);
        return thread;
    }

    private Thread createTask(File file) {
        Thread thread = new Thread(() -> {
            progressContext.setRunLater(true);
            progressContext.step("Start write to JAR pack");
            try {
                write(file);
                progressContext.step("Write to JAR pack finish");
                progressContext.end(true);
            } catch (Exception e) {
                progressContext.step(ExceptionUtil.asString(e));
                progressContext.end(false);
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
            throw new AppException("Generate Application pack fail", e);
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
        progressContext.step("Check Docker server info");
        String host = docker.getHost();
        String username = docker.getUsername();
        String password = docker.getPassword();
        if (StringUtil.isBlank(host) || StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
            throw new AppException("Docker server info error");
        }
        progressContext.step("SSH-connect: " + docker.getHost());
        Session session = null;
        try {
            password = EncryptUtil.decryptWithRsaSys(password);
            session = SSHUtil.createSession(host, docker.getPort(), username, password);
            progressContext.step("The SSH connection is successful");
            OutputStream progressOut = progressContext.createOutputStream();
            String command = StringUtil.isBlank(docker.getCommand()) ? DockerConf.DEFAULT_COMMAND : docker.getCommand();
            String workPath = StringUtil.isBlank(docker.getWorkPath()) ? DockerConf.DEFAULT_WORK_PATH : docker.getWorkPath();
            progressContext.step("Upload the Dockerfile");
            SSHUtil.uploadFile(session, progressOut, createSftpProgressMonitor(true), workPath, AppConst.DOCKERFILE_FILE_NAME, dockerfile.getBytes());
            progressContext.step("Upload the app package:" + rootNode.getName());
            SSHUtil.uploadFile(session, progressOut, createSftpProgressMonitor(true), workPath, rootNode.getName(), bytes);
            progressContext.step("Generate a Docker image");
            int status = SSHUtil.execCmd(session, progressOut, command + " build -t demo:1.0.0 " + workPath);
            if (status != 0) {
                throw new AppException("Failed to generate a Docker image");
            }
            progressContext.step("Export a Docker image");
            status = SSHUtil.execCmd(session, progressOut, command + " save -o " + workPath + "/demo.tar demo:1.0.0");
            if (status != 0) {
                throw new AppException("Failed to export a Docker image");
            }
            progressContext.step("Download the Docker image");
            SSHUtil.downloadFile(session, progressOut, createSftpProgressMonitor(false), workPath + "/demo.tar", file);
        } finally {
            progressContext.step("Close the SSH connection");
            SSHUtil.closeSession(session);
        }
    }

    private void writeDockerWithLocal(DockerConf docker, File file, TreeNode rootNode, byte[] bytes, String dockerfile) {
        String localCommand = StringUtil.isBlank(docker.getLocalCommand()) ? DockerConf.DEFAULT_COMMAND : docker.getLocalCommand();
        String localWorkPath = StringUtil.isBlank(docker.getLocalWorkPath()) ? DockerConf.DEFAULT_WORK_PATH : docker.getLocalWorkPath();
        progressContext.step("Write Dockerfile to:" + localWorkPath);
        FileUtil.writeFile(localWorkPath, AppConst.DOCKERFILE_FILE_NAME, dockerfile.getBytes());
        progressContext.step("Write Application Pack to: " + localWorkPath);
        FileUtil.writeFile(localWorkPath, rootNode.getName(), bytes);
        progressContext.step("Generate Docker image");
        OutputStream progressOut = progressContext.createOutputStream();
        int status = CommandUtil.exec(localCommand + " build -t demo:1.0.0 .", localWorkPath, progressOut);
        if (status != 0) {
            throw new AppException("Generate Docker image fail");
        }
        progressContext.step("Export Docker image");
        status = CommandUtil.exec(localCommand + " save -o demo.tar demo:1.0.0", localWorkPath, progressOut);
        if (status != 0) {
            throw new AppException("Export Docker image fail");
        }
        progressContext.step("Move Docker image");
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
                String message = isUpload ? "Upload " : "Download ";
                if (!StringUtil.isBlank(src)) {
                    message += src + " -> ";
                }
                if (!StringUtil.isBlank(dest)) {
                    message += dest;
                }
                if (max > 0) {
                    message += " Size:" + FileUtil.getSizeReadability(max);
                }
                progressContext.step(message);
            }

            @Override
            public boolean count(long count) {
                if (opCount == 0) {
                    progressContext.step((isUpload ? "Uploaded " : "Downloaded ") + FileUtil.getSizeReadability(opCount += count));
                } else {
                    progressContext.overwrite((isUpload ? "Uploaded " : "Downloaded ") + FileUtil.getSizeReadability(opCount += count));
                }
                return true;

            }

            @Override
            public void end() {
                String message = isUpload ? "Upload " : "Download ";
                if (!StringUtil.isBlank(src)) {
                    message += src + " -> ";
                }
                if (!StringUtil.isBlank(desc)) {
                    message += desc;
                }
                progressContext.step(message + " finish");
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
            throw new AppException("Write fail", e);
        }
    }

    private void doWrite(TreeNode rootNode, ZipOutputStream zipOut) throws IOException {
        progressContext.step("Start write");
        boolean hasMetaInfoNode = false;
        TreeNode patchUpSignNode = context.getAppTree().getTreeInfo().getPatchUpSignNode();
        boolean isPatchSign = Boolean.TRUE.equals(Configuration.getInstance().getPatch().getWritePatchSign());
        if (rootNode.getChildren() != null) {
            progressContext.step("Application pack name:" + rootNode.getName());
            for (TreeNode child : rootNode.getChildren()) {
                writeTreeNode2Pack(zipOut, child);
                // 需要写入补丁签名的情况
                if (JarInfoConst.META_INFO_DIR.equals(child.getPath()) && (isPatchSign || patchUpSignNode != null)) {
                    hasMetaInfoNode = true;
                    writePatchSign(zipOut, patchUpSignNode, isPatchSign);
                }
            }
        }
        if (!hasMetaInfoNode && (isPatchSign || patchUpSignNode != null)) {
            ZipEntry zipEntry = new ZipEntry(JarInfoConst.META_INFO_DIR);
            zipOut.putNextEntry(zipEntry);
            writePatchSign(zipOut, patchUpSignNode, isPatchSign);
        }
        zipOut.finish();
        progressContext.step("Write finished");
    }

    private void writePatchSign(ZipOutputStream zipOut, TreeNode patchUpSignNode, boolean isPatchSign) throws IOException {
        if (patchUpSignNode instanceof ZipEntryNode) {
            ZipEntry zipEntry = getNewEntryWithZipEntry(patchUpSignNode);
            writePatchSign(zipOut, zipEntry, isPatchSign, patchUpSignNode.getBytes());
            return;
        }
        ZipEntry zipEntry = new ZipEntry(JarInfoConst.META_INFO_DIR + AppConst.PATCH_UP_SIGN);
        writePatchSign(zipOut, zipEntry, isPatchSign, patchUpSignNode == null ? null : patchUpSignNode.getBytes());
    }

    private void writePatchSign(ZipOutputStream zipOut, ZipEntry zipEntry, boolean isPatchSign, byte[] bytes) throws IOException {
        zipOut.putNextEntry(zipEntry);
        if (isPatchSign) {
            bytes = updatePatchSignContent(bytes);
        }
        zipOut.write(bytes);
    }

    private byte[] updatePatchSignContent(byte[] bytes) {
        PatchUpSign patchUpSign = PatchUpSign.of(context.getPatchTree().getTreeInfo().getPatchSign());
        TotalInfo totalInfo = context.getTotalInfo();
        patchUpSign.setAddTotal(totalInfo.getAddTotal());
        patchUpSign.setModTotal(totalInfo.getModTotal());
        patchUpSign.setDelTotal(totalInfo.getDelTotal());
        patchUpSign.setManualDelTotal(totalInfo.getManualDelTotal());
        patchUpSign.setOperTime(new Date());
        patchUpSign.setOperUser(EnvUtil.getUserName());
        AppTreeInfo treeInfo = context.getAppTree().getTreeInfo();
        AppPackSign appPackSign = treeInfo.getAppPackSign();
        patchUpSign.setOriginAppMd5(appPackSign.getMd5());
        patchUpSign.setOriginAppSha1(appPackSign.getSha1());
        patchUpSign.setOriginAppSize(treeInfo.getRootNode().getSize());
        if (bytes == null) {
            return JsonUtil.writeAsBytes(Collections.singletonList(patchUpSign));
        }
        List<PatchUpSign> patchUpSigns = JsonUtil.read(bytes, new TypeReference<>() {
        });
        patchUpSigns.add(0, patchUpSign);
        return JsonUtil.writeAsBytes(patchUpSigns);
    }

    private void writeTreeNode2Pack(ZipOutputStream zipOut, TreeNode node) throws IOException {
        // 标记为删除状态的节点不在写入新的包中
        TreeNodeStatus status = node.getStatus();
        if (TreeNodeStatus.DEL.equals(status) || TreeNodeStatus.MANUAL_DEL.equals(status)) {
            progressContext.step("Delete:" + node.getName());
            return;
        }
        if (!node.isDir() && node.getText().endsWith(FileExtConst.DOT_JAR)) {
            progressContext.step("Write:" + node.getName());
            writeTreeNode2PackWithJar(zipOut, (ZipEntryNode) node);
            return;
        }
        if (node.getMappedNode() == null) {
            zipOut.putNextEntry(getNewEntryWithZipEntry(node));
            if (!node.isDir()) {
                progressContext.step("Write:" + node.getName());
                zipOut.write(node.getBytes());
            }
        } else {
            TreeNode mappedNode = node.getMappedNode();
            ZipEntry zipEntry = getNewEntryWithZipEntry(node);
            zipEntry.setTimeLocal(mappedNode.getModifyTime());
            zipOut.putNextEntry(zipEntry);
            if (!zipEntry.isDirectory()) {
                progressContext.step("Write:" + node.getName());
                zipOut.write(mappedNode.getBytes());
            }
        }
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                writeTreeNode2Pack(zipOut, child);
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
                writeTreeNode2Pack(zipOut, child);
            }
            zipOut.finish();
            return out.toByteArray();
        }
    }

    private ZipEntry getNewEntryWithZipEntry(TreeNode node) {
        if (node instanceof ZipEntryNode) {
            return getNewEntryWithZipEntry(((ZipEntryNode) node).getEntry());
        }
        ZipEntry entry = new ZipEntry(node.getPath());
        if (node.getName().endsWith(FileExtConst.DOT_JAR)) {
            entry.setMethod(ZipEntry.STORED);
        }
        return entry;
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
