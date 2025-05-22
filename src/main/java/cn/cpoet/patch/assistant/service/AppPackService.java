package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.view.tree.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        rootNode.setPath(file.getPath());
        rootNode.setFile(file);
        treeInfo.setRootNode(rootNode);
        try (InputStream in = new FileInputStream(file);
             ZipInputStream zin = new ZipInputStream(in)) {
            doReadZipEntry(rootNode, zin);
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
             JarOutputStream jarOut = new JarOutputStream(out)) {
            if (rootNode.getChildren() != null) {
                for (TreeNode child : rootNode.getChildren()) {
                    writeTreeNode2Pack(jarOut, (ZipEntryNode) child);
                }
            }
        } catch (Exception e) {
            throw new AppException("写入应用包失败", e);
        }
    }

    protected void writeTreeNode2Pack(JarOutputStream jarOut, ZipEntryNode node) throws IOException {
        if (!node.isDir() && node.getName().endsWith(FileExtConst.DOT_JAR)) {
            writeTreeNode2PackWithJar(jarOut, node);
            return;
        }
        if (node.getMappedNode() == null) {
            jarOut.putNextEntry(getJarEntryWithZipEntry(node.getEntry()));
            if (!node.isDir()) {
                jarOut.write(node.getBytes());
            }
        } else {
            TreeKindNode mappedNode = (TreeKindNode) node.getMappedNode();
            JarEntry jarEntry = getJarEntryWithZipEntry(node.getEntry());
            jarEntry.setTimeLocal(mappedNode.getModifyTime());
            jarOut.putNextEntry(jarEntry);
            if (!jarEntry.isDirectory()) {
                jarOut.write(mappedNode.getBytes());
            }
        }
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                writeTreeNode2Pack(jarOut, (ZipEntryNode) child);
            }
        }
    }

    protected void writeTreeNode2PackWithJar(JarOutputStream jarOut, ZipEntryNode node) throws IOException {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            jarOut.putNextEntry(getJarEntryWithZipEntry(node.getEntry()));
            jarOut.write(node.getBytes());
            return;
        }
        byte[] bytes = getBytesWithJarNode(node);
        JarEntry jarEntry = getJarEntryWithZipEntry(node.getEntry());
        jarEntry.setTimeLocal(LocalDateTime.now());
        jarOut.putNextEntry(jarEntry);
        jarOut.write(bytes);
    }

    protected byte[] getBytesWithJarNode(ZipEntryNode jarNode) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             JarOutputStream jarOut = new JarOutputStream(out)) {
            for (TreeNode child : jarNode.getChildren()) {
                writeTreeNode2Pack(jarOut, (ZipEntryNode) child);
            }
            return out.toByteArray();
        }
    }

    protected JarEntry getJarEntryWithZipEntry(ZipEntry zipEntry) {
        JarEntry jarEntry = new JarEntry(zipEntry.getName());
        jarEntry.setComment(zipEntry.getComment());
        if (zipEntry.getCreationTime() != null) {
            jarEntry.setCreationTime(zipEntry.getCreationTime());
        }
        if (zipEntry.getLastAccessTime() != null) {
            jarEntry.setLastAccessTime(zipEntry.getLastAccessTime());
        }
        jarEntry.setLastModifiedTime(zipEntry.getLastModifiedTime());
        return jarEntry;
    }
}
