package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.common.InputBufConsumer;
import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.JarInfoConst;
import cn.cpoet.patch.assistant.control.tree.*;
import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.PatchConf;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.model.AppPackSign;
import cn.cpoet.patch.assistant.model.PatchSign;
import cn.cpoet.patch.assistant.model.PatchUpSign;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.home.HomeContext;
import cn.cpoet.patch.assistant.view.progress.ProgressContext;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 打补丁处理
 *
 * @author CPoet
 */
public class AppPackWriteProcessor {

    private final HomeContext context;
    private final ProgressContext progressContext;

    public AppPackWriteProcessor(HomeContext context, ProgressContext progressContext) {
        this.context = context;
        this.progressContext = progressContext;
    }

    private File createTransitFile(File file) {
        try {
            return new File(file.getParentFile(), file.getName() + FileTempUtil.TEMP_FILE_EXT);
        } catch (Exception ignored) {
        }
        return FileTempUtil.createTempFile(file.getName());
    }

    public void exec(File file) {
        File transitFile = createTransitFile(file);
        createTask(file, transitFile);
    }

    private void transitCompleted(File file, File transitFile, boolean isOk) {
        if (isOk) {
            progressContext.step("Write to:" + file.getPath());
            try {
                FileUtil.copyTo(transitFile, file);
            } catch (Exception e) {
                throw new AppException("Write file failed", e);
            } finally {
                progressContext.step("Clean transit file");
                FileTempUtil.deleteTempFile(transitFile);
            }
        } else {
            progressContext.step("Clean transit file");
            FileTempUtil.deleteTempFile(transitFile);
        }
    }

    private void createTask(File file, File transitFile) {
        UIUtil.runNotUI(() -> {
            progressContext.step("Start write to JAR pack");
            try {
                write(transitFile);
                progressContext.step("Write to JAR pack finish");
                transitCompleted(file, transitFile, true);
                progressContext.end(true);
            } catch (Exception e) {
                progressContext.step(e.getMessage());
                System.err.println(ExceptionUtil.asString(e));
                transitCompleted(file, transitFile, false);
                progressContext.end(false);
            }
        });
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
        if (patchUpSignNode instanceof CompressNode) {
            ZipEntry zipEntry = getNewEntryWithZipEntry(patchUpSignNode);
            zipEntry.setLastModifiedTime(DateUtil.toFileTimeOrCur(null));
            writePatchSign(zipOut, zipEntry, isPatchSign, TreeNodeUtil.readNodeBytes(patchUpSignNode));
            return;
        }
        ZipEntry zipEntry = new ZipEntry(JarInfoConst.META_INFO_DIR + AppConst.PATCH_UP_SIGN);
        writePatchSign(zipOut, zipEntry, isPatchSign, patchUpSignNode == null ? null : TreeNodeUtil.readNodeBytes(patchUpSignNode));
    }

    private void writePatchSign(ZipOutputStream zipOut, ZipEntry zipEntry, boolean isPatchSign, byte[] bytes) throws IOException {
        zipOut.putNextEntry(zipEntry);
        if (isPatchSign) {
            bytes = updatePatchSignContent(bytes);
        }
        zipOut.write(bytes == null ? new byte[0] : bytes);
    }

    private byte[] updatePatchSignContent(byte[] bytes) {
        PatchConf patchConf = Configuration.getInstance().getPatch();
        PatchTreeInfo patchTreeInfo = context.getPatchTree().getTreeInfo();
        PatchUpSign patchUpSign = patchTreeInfo == null ? new PatchUpSign() : PatchUpSign.of(patchTreeInfo.getRootInfo().getPatchSign());
        TotalInfo totalInfo = context.getTotalInfo();
        patchUpSign.setAddTotal(totalInfo.getAddTotal());
        patchUpSign.setModTotal(totalInfo.getModTotal());
        patchUpSign.setDelTotal(totalInfo.getDelTotal());
        patchUpSign.setManualDelTotal(totalInfo.getManualDelTotal());
        patchUpSign.setOperTime(new Date());
        patchUpSign.setOperUser(patchConf.getUsername());
        AppTreeInfo treeInfo = context.getAppTree().getTreeInfo();
        AppPackSign appPackSign = treeInfo.getAppPackSign();
        patchUpSign.setOriginAppMd5(appPackSign.getMd5());
        patchUpSign.setOriginAppSha1(appPackSign.getSha1());
        patchUpSign.setOriginAppSize(treeInfo.getRootNode().getSize());
        if (patchTreeInfo != null) {
            Map<TreeNode, PatchRootInfo> customRootInfoMap = patchTreeInfo.getCustomRootInfoMap();
            if (CollectionUtil.isNotEmpty(customRootInfoMap)) {
                List<PatchSign> patchSigns = customRootInfoMap.values().stream().map(PatchRootInfo::getPatchSign).collect(Collectors.toList());
                patchUpSign.setSigns(patchSigns);
            }
        }
        List<PatchUpSign> patchUpSigns = null;
        if (bytes != null && bytes.length > 0) {
            try {
                patchUpSigns = JsonUtil.read(bytes, new TypeReference<>() {
                });
                patchUpSigns.add(0, patchUpSign);
            } catch (Exception ignored) {
            }
        }
        return patchUpSigns == null ? JsonUtil.writeAsBytes(Collections.singletonList(patchUpSign)) : JsonUtil.writeAsBytes(patchUpSigns);
    }

    private void writeTreeNode2Pack(ZipOutputStream zipOut, TreeNode node) throws IOException {
        // 标记为删除状态的节点不在写入新的包中
        TreeNodeType status = node.getType();
        if (TreeNodeType.DEL.equals(status)) {
            progressContext.step("Delete:" + node.getName());
            return;
        }
        if (!node.isDir() && node.getName().endsWith(FileExtConst.DOT_JAR)) {
            progressContext.step("Write:" + node.getName());
            writeTreeNode2PackWithJar(zipOut, node);
            return;
        }
        if (node.getMappedNode() == null) {
            zipOut.putNextEntry(getNewEntryWithZipEntry(node));
            if (!node.isDir()) {
                progressContext.step("Write:" + node.getName());
                node.consumeBytes(((len, buf) -> zipOut.write(buf, 0, len)));
            }
        } else {
            TreeNode mappedNode = node.getMappedNode();
            ZipEntry zipEntry = getNewEntryWithZipEntry(node);
            zipEntry.setTimeLocal(mappedNode.getModifyTime());
            zipOut.putNextEntry(zipEntry);
            if (!zipEntry.isDirectory()) {
                progressContext.step("Write:" + node.getName());
                mappedNode.consumeBytes(((len, buf) -> zipOut.write(buf, 0, len)));
            }
        }
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            for (TreeNode child : node.getChildren()) {
                writeTreeNode2Pack(zipOut, child);
            }
        }
    }

    private void writeTreeNode2PackWithJar(ZipOutputStream zipOut, TreeNode node) throws IOException {
        if (CollectionUtil.isEmpty(node.getChildren())) {
            zipOut.putNextEntry(getNewEntryWithZipEntry(node));
            node.consumeBytes(((len, buf) -> zipOut.write(buf, 0, len)));
            return;
        }
        File file = getTempFileWithJarNode(node);
        try {
            ZipEntry zipEntry = getNewEntryWithZipEntry(node);
            int[] size = new int[]{0};
            CRC32 crc32 = new CRC32();
            FileUtil.readBuf(file, ((len, buf) -> {
                size[0] += len;
                crc32.update(buf, 0, len);
            }));
            zipEntry.setSize(size[0]);
            zipEntry.setCrc(crc32.getValue());
            zipEntry.setTimeLocal(LocalDateTime.now());
            zipOut.putNextEntry(zipEntry);
            FileUtil.readBuf(file, ((len, buf) -> zipOut.write(buf, 0, len)));
        } finally {
            FileTempUtil.deleteTempFile(file);
        }
    }

    private File getTempFileWithJarNode(TreeNode jarNode) throws IOException {
        File file = new File(AppContext.getInstance().getTempDir(), FileNameUtil.uniqueFileName(jarNode.getName()));
        try (OutputStream out = new FileOutputStream(file);
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (TreeNode child : jarNode.getChildren()) {
                writeTreeNode2Pack(zipOut, child);
            }
            zipOut.finish();
            return file;
        }
    }

    private ZipEntry getNewEntryWithZipEntry(TreeNode node) {
        ZipEntry entry = new ZipEntry(node.isDir() ? FileNameUtil.perfectDirPath(node.getPath()) : node.getPath());
        if (!node.isDir()) {
            long sizeOrInit = node.getSizeOrInit();
           if (sizeOrInit <= 0) {
               System.out.println(sizeOrInit);
           }
            entry.setSize(node.getSizeOrInit());
            if (node.getName().endsWith(FileExtConst.DOT_JAR)) {
                entry.setMethod(ZipEntry.STORED);
            }
        }
        if (node instanceof CompressNode) {
            CompressNode cNode = (CompressNode) node;
            entry.setComment(cNode.getComment());
            entry.setCreationTime(DateUtil.toFileTimeOrCur(cNode.getCreateTime()));
            entry.setLastAccessTime(DateUtil.toFileTimeOrCur(cNode.getAccessTime()));
            entry.setLastModifiedTime(DateUtil.toFileTimeOrCur(cNode.getModifyTime()));
            if (cNode.getCrc() != -1) {
                entry.setCrc(cNode.getCrc());
            }
            entry.setExtra(cNode.getExtra());
        }
        if (entry.getMethod() == ZipEntry.STORED && entry.getCrc() < 0) {
            updateCrc32(entry, node::consumeBytes);
        }
        return entry;
    }

    private void updateCrc32(ZipEntry entry, Consumer<InputBufConsumer> consumer) {
        CRC32 crc32 = new CRC32();
        consumer.accept(((len, buf) -> crc32.update(buf, 0, len)));
        entry.setCrc(crc32.getValue());
    }
}
