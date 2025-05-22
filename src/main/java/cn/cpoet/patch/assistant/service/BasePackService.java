package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.ZipEntryNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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
        if (!(rootNode instanceof TreeKindNode)) {
            return false;
        }
        if (rootNode.getChildren() != null) {
            return false;
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(((TreeKindNode) rootNode).getBytes());
             ZipInputStream zin = new ZipInputStream(in, Charset.forName("GBK"))) {
            doReadZipEntry(rootNode, zin);
            return true;
        } catch (IOException ex) {
            throw new AppException("读取压缩文件失败", ex);
        }
    }

    protected void doReadZipEntry(TreeNode rootNode, ZipInputStream zin) throws IOException {
        ZipEntry zipEntry;
        TreeNode manifestNode = null;
        Map<String, TreeNode> treeNodeMap = new HashMap<>();
        while ((zipEntry = zin.getNextEntry()) != null) {
            ZipEntryNode zipEntryNode = new ZipEntryNode();
            zipEntryNode.setName(FileNameUtil.getFileName(zipEntry.getName()));
            zipEntryNode.setPath(zipEntry.getName());
            zipEntryNode.setEntry(zipEntry);
            if (!zipEntry.isDirectory()) {
                zipEntryNode.setSize(zipEntry.getSize());
                zipEntryNode.setBytes(zin.readAllBytes());
            }
            if ("META-INF/MANIFEST.MF".equals(zipEntry.getName())) {
                manifestNode = zipEntryNode;
                continue;
            }
            if ("META-INF/".equals(zipEntry.getName())) {
                if (manifestNode != null) {
                    manifestNode.setParent(zipEntryNode);
                    zipEntryNode.getAndInitChildren().add(manifestNode);
                }
            }
            TreeNode parentNode = treeNodeMap.getOrDefault(FileNameUtil.getDirPath(zipEntry.getName()), rootNode);
            zipEntryNode.setParent(parentNode);
            parentNode.getAndInitChildren().add(zipEntryNode);
            if (zipEntry.isDirectory()) {
                treeNodeMap.put(zipEntry.getName().substring(0, zipEntry.getName().length() - 1), zipEntryNode);
            }
        }
    }
}
