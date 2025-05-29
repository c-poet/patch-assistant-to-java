package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.JarInfoConst;
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

    public boolean buildNodeChildrenWithZip(TreeNode rootNode, boolean isPatch) {
        if (!(rootNode instanceof TreeKindNode)) {
            return false;
        }
        if (rootNode.getChildren() != null) {
            return false;
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(((TreeKindNode) rootNode).getBytes());
             ZipInputStream zin = new ZipInputStream(in, Charset.forName("GBK"))) {
            doReadZipEntry(rootNode, zin, isPatch);
            return true;
        } catch (IOException ex) {
            throw new AppException("读取压缩文件失败", ex);
        }
    }

    protected void doReadZipEntry(TreeNode rootNode, ZipInputStream zin, boolean isPatch) throws IOException {
        ZipEntry zipEntry;
        TreeNode manifestNode = null;
        Map<String, TreeNode> treeNodeMap = new HashMap<>();
        while ((zipEntry = zin.getNextEntry()) != null) {
            ZipEntryNode zipEntryNode = new ZipEntryNode();
            zipEntryNode.setText(FileNameUtil.getFileName(zipEntry.getName()));
            zipEntryNode.setPath(zipEntry.getName());
            zipEntryNode.setEntry(zipEntry);
            zipEntryNode.setPatch(isPatch);
            if (!zipEntry.isDirectory()) {
                zipEntryNode.setSize(zipEntry.getSize());
                zipEntryNode.setBytes(zin.readAllBytes());
            }
            TreeNode parentNode = treeNodeMap.getOrDefault(FileNameUtil.getDirPath(zipEntry.getName()), rootNode);
            if (JarInfoConst.MANIFEST_PATH.equals(zipEntry.getName())) {
                if (parentNode != rootNode) {
                    zipEntryNode.setParent(parentNode);
                    parentNode.getAndInitChildren().add(zipEntryNode);
                } else {
                    manifestNode = zipEntryNode;
                }
                continue;
            }
            if (JarInfoConst.META_INFO_DIR.equals(zipEntry.getName())) {
                if (manifestNode != null) {
                    manifestNode.setParent(zipEntryNode);
                    zipEntryNode.getAndInitChildren().add(manifestNode);
                }
            }
            zipEntryNode.setParent(parentNode);
            parentNode.getAndInitChildren().add(zipEntryNode);
            if (zipEntry.isDirectory()) {
                treeNodeMap.put(zipEntry.getName().substring(0, zipEntry.getName().length() - 1), zipEntryNode);
            }
        }
    }
}
