package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.view.tree.*;

import java.io.*;
import java.time.LocalDateTime;
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

    /**
     * 生成并保存应用包
     */
    public void savePack(File file, TreeInfo appTree) {
        TreeNode rootNode = appTree.getRootNode();
        try (OutputStream out = new FileOutputStream(file);
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            if (rootNode.getChildren() != null) {
                for (TreeNode child : rootNode.getChildren()) {
                    writeTreeNode2Pack(zipOut, (ZipEntryNode) child);
                }
            }
            zipOut.finish();
        } catch (Exception e) {
            throw new AppException("写入应用包失败", e);
        }
    }

    protected void writeTreeNode2Pack(ZipOutputStream zipOut, ZipEntryNode node) throws IOException {
        // 标记为删除状态的节点不在写入新的包中
        TreeNodeStatus status = node.getStatus();
        if (TreeNodeStatus.DEL.equals(status) || TreeNodeStatus.MARK_DEL.equals(status)) {
            return;
        }
        if (!node.isDir() && node.getText().endsWith(FileExtConst.DOT_JAR)) {
            writeTreeNode2PackWithJar(zipOut, node);
            return;
        }
        if (node.getMappedNode() == null) {
            zipOut.putNextEntry(getNewEntryWithZipEntry(node.getEntry()));
            if (!node.isDir()) {
                zipOut.write(node.getBytes());
            }
        } else {
            TreeKindNode mappedNode = (TreeKindNode) node.getMappedNode();
            ZipEntry zipEntry = getNewEntryWithZipEntry(node.getEntry());
            zipEntry.setTimeLocal(mappedNode.getModifyTime());
            zipOut.putNextEntry(zipEntry);
            if (!zipEntry.isDirectory()) {
                zipOut.write(mappedNode.getBytes());
            }
        }
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                writeTreeNode2Pack(zipOut, (ZipEntryNode) child);
            }
        }
    }

    protected void writeTreeNode2PackWithJar(ZipOutputStream zipOut, ZipEntryNode node) throws IOException {
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

    protected byte[] getBytesWithJarNode(ZipEntryNode jarNode) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (TreeNode child : jarNode.getChildren()) {
                writeTreeNode2Pack(zipOut, (ZipEntryNode) child);
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
