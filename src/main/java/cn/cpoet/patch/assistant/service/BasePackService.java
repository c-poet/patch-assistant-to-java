package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.HashUtil;
import cn.cpoet.patch.assistant.view.tree.FileNode;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.ZipEntryNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 压缩包处理基类
 *
 * @author CPoet
 */
public abstract class BasePackService {

    public boolean buildNodeChildrenWithZip(TreeNode rootNode) {
        if (!(rootNode instanceof FileNode)) {
            return false;
        }
        if (rootNode.getChildren() != null && !rootNode.getChildren().isEmpty()) {
            return false;
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(((FileNode) rootNode).getBytes());
             ZipInputStream zin = new ZipInputStream(in)) {
            doReadZipEntry(rootNode, zin);
            return true;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void doReadZipEntry(TreeNode rootNode, ZipInputStream zin) throws IOException {
        ZipEntry zipEntry;
        Map<String, TreeNode> treeNodeMap = new HashMap<>();
        while ((zipEntry = zin.getNextEntry()) != null) {
            ZipEntryNode zipEntryNode = new ZipEntryNode();
            zipEntryNode.setName(FileNameUtil.getFileName(zipEntry.getName()));
            zipEntryNode.setPath(zipEntry.getName());
            zipEntryNode.setEntry(zipEntry);
            if (!zipEntry.isDirectory()) {
                zipEntryNode.setBytes(zin.readAllBytes());
            }
            TreeNode parentNode = treeNodeMap.getOrDefault(FileNameUtil.getDirPath(zipEntry.getName()), rootNode);
            if (parentNode.getChildren() == null) {
                parentNode.setChildren(new ArrayList<>());
            }
            zipEntryNode.setParent(parentNode);
            parentNode.getChildren().add(zipEntryNode);
            if (zipEntry.isDirectory()) {
                treeNodeMap.put(zipEntry.getName().substring(0, zipEntry.getName().length() - 1), zipEntryNode);
            }
            zin.closeEntry();
        }
    }
}
