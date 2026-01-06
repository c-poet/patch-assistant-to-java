package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.common.InputBufConsumer;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.JarInfoConst;
import cn.cpoet.patch.assistant.control.tree.*;
import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.PatchConf;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.model.*;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.home.HomeContext;
import cn.cpoet.patch.assistant.view.progress.ProgressContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 打补丁处理
 *
 * @author CPoet
 */
public class AppPackWriteProcessor {

    private List<DelInfo> delInfos;
    private List<AddInfo> addInfos;
    private List<ModInfo> modInfos;
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
        boolean isWritePathSign = false;
        if (rootNode.getChildren() != null) {
            progressContext.step("Application pack name:" + rootNode.getName());
            for (TreeNode child : rootNode.getChildren()) {
                if (JarInfoConst.META_INFO_DIR.equals(child.getPath()) && isWritePatchSign()) {
                    writePatchSign(zipOut, child);
                    isWritePathSign = true;
                }
                writeTreeNode2Pack(zipOut, child);
            }
        }
        if (!isWritePathSign && isWritePatchSign()) {
            writePatchSign(zipOut, null);
        }
        zipOut.finish();
        progressContext.step("Write finished");
    }

    private void writePatchSign(ZipOutputStream zipOut, TreeNode metaInfoNode) throws IOException {
        int targetNo = 1;
        TreeNode patchSignNode = null;
        String fileName = DateUtil.curDatePure();
        if (metaInfoNode == null) {
            zipOut.putNextEntry(new ZipEntry(JarInfoConst.META_INFO_DIR));
        } else if (CollectionUtil.isNotEmpty(metaInfoNode.getChildren())) {
            for (TreeNode child : metaInfoNode.getChildren()) {
                if (JarInfoConst.PATCH_UP_PATH.equals(child.getPath())) {
                    patchSignNode = child;
                    break;
                }
            }
        }
        if (patchSignNode == null) {
            zipOut.putNextEntry(new ZipEntry(JarInfoConst.PATCH_UP_PATH));
        } else if (CollectionUtil.isNotEmpty(patchSignNode.getChildren())) {
            Set<Integer> existsNoSet = patchSignNode.getChildren()
                    .stream()
                    .map(TreeNode::getName)
                    .filter(name -> name.startsWith(fileName))
                    .map(name -> {
                        try {
                            return Integer.parseInt(name.substring(0, name.length() - FileExtConst.DOT_SIGN.length()).substring(fileName.length()));
                        } catch (Exception ignored) {
                        }
                        return 0;
                    }).collect(Collectors.toSet());
            if (CollectionUtil.isNotEmpty(existsNoSet)) {
                while (existsNoSet.contains(targetNo)) {
                    ++targetNo;
                }
            }
        }
        String name = fileName + (targetNo > 9 ? targetNo : "0" + targetNo) + FileExtConst.DOT_SIGN;
        ZipEntry zipEntry = new ZipEntry(FileNameUtil.joinPath(JarInfoConst.PATCH_UP_PATH, name));
        doWritePatchSign(zipOut, zipEntry);
    }

    private void doWritePatchSign(ZipOutputStream zipOut, ZipEntry zipEntry) throws IOException {
        zipOut.putNextEntry(zipEntry);
        byte[] patchSignContent = createPatchSignContent();
        zipOut.write(patchSignContent);
    }

    private byte[] createPatchSignContent() {
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
        patchUpSign.setDelInfos(delInfos);
        patchUpSign.setAddInfos(addInfos);
        patchUpSign.setModInfos(modInfos);
        if (patchTreeInfo != null) {
            Map<TreeNode, PatchRootInfo> customRootInfoMap = patchTreeInfo.getCustomRootInfoMap();
            if (CollectionUtil.isNotEmpty(customRootInfoMap)) {
                List<PatchSign> patchSigns = customRootInfoMap.values().stream().map(PatchRootInfo::getPatchSign).collect(Collectors.toList());
                patchUpSign.setSigns(patchSigns);
            }
        }
        return JsonUtil.writeAsBytes(patchUpSign);
    }

    private void writeTreeNode2Pack(ZipOutputStream zipOut, TreeNode node) throws IOException {
        // 标记为删除状态的节点不在写入新的包中
        TreeNodeType status = node.getType();
        if (TreeNodeType.DEL.equals(status)) {
            if (!node.isDir()) {
                addDelInfo(node);
            }
            progressContext.step("Delete:" + node.getName());
            return;
        }
        if (!node.isDir() && node.getName().endsWith(FileExtConst.DOT_JAR)) {
            progressContext.step("Write:" + node.getName());
            writeTreeNode2PackWithJar(zipOut, node);
            if (node.getMappedNode() != null) {
                if (TreeNodeType.ADD.equals(node.getType())) {
                    addAddInfo(node, node.getMappedNode());
                } else {
                    addModInfo(node, node.getMappedNode());
                }
            }
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
                if (TreeNodeType.ADD.equals(node.getType())) {
                    addAddInfo(node, mappedNode);
                } else {
                    addModInfo(node, mappedNode);
                }
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

    private void addDelInfo(TreeNode appNode) {
        if (isWritePatchSign()) {
            if (delInfos == null) {
                delInfos = new LinkedList<>();
            }
            DelInfo delInfo = new DelInfo();
            delInfo.setAppPath(appNode.getPath());
            delInfo.setAppMd5(appNode.getMd5OrInit());
            delInfo.setAppCreateTime(DateUtil.formatDateTime(appNode.getModifyTime()));
            delInfos.add(delInfo);
        }
    }

    private void addAddInfo(TreeNode appNode, TreeNode patchNode) {
        if (isWritePatchSign()) {
            if (addInfos == null) {
                addInfos = new LinkedList<>();
            }
            AddInfo addInfo = new AddInfo();
            addInfo.setAppPath(appNode.getPath());
            addInfo.setPatchPath(patchNode.getPath());
            addInfo.setPatchMd5(patchNode.getMd5OrInit());
            addInfo.setPatchCreateTime(DateUtil.formatDateTime(patchNode.getModifyTime()));
            addInfos.add(addInfo);
        }
    }

    private void addModInfo(TreeNode appNode, TreeNode patchNode) {
        if (isWritePatchSign()) {
            if (modInfos == null) {
                modInfos = new LinkedList<>();
            }
            ModInfo modInfo = new ModInfo();
            modInfo.setAppPath(appNode.getPath());
            modInfo.setAppMd5(appNode.getMd5OrInit());
            modInfo.setPatchPath(patchNode.getPath());
            modInfo.setPatchMd5(patchNode.getMd5OrInit());
            modInfo.setAppCreateTime(DateUtil.formatDateTime(appNode.getModifyTime()));
            modInfo.setPatchCreateTime(DateUtil.formatDateTime(patchNode.getModifyTime()));
            modInfos.add(modInfo);
        }
    }

    private boolean isWritePatchSign() {
        return Boolean.TRUE.equals(Configuration.getInstance().getPatch().getWritePatchSign());
    }
}
