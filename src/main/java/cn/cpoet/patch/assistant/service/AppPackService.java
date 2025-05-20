package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.view.tree.FileNode;
import cn.cpoet.patch.assistant.view.tree.TreeInfo;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.ZipEntryNode;

import java.io.*;
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
        rootNode.setPath(file.getPath());
        treeInfo.setRootNode(rootNode);
        try (InputStream in = new FileInputStream(file);
             ZipInputStream zin = new ZipInputStream(in)) {
            doReadZipEntry(rootNode, zin);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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
                rootNode.getChildren().forEach(node -> writeTreeNode2Pack(zipOut, node));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeTreeNode2Pack(ZipOutputStream zipOut, TreeNode node) {
        TreeNode targetNode = node;
        if (node.getMappedNode() != null) {
            targetNode = node.getMappedNode();
            if (!Boolean.TRUE.equals(targetNode.getChecked())) {
                return;
            }
        }
        try {
            if (targetNode instanceof ZipEntryNode) {
                ZipEntryNode zipEntryNode = (ZipEntryNode) targetNode;
                zipOut.putNextEntry(zipEntryNode.getEntry());
                if (!zipEntryNode.getEntry().isDirectory()) {
                    zipOut.write(zipEntryNode.getBytes());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (node.getChildren() != null) {
            node.getChildren().forEach(child -> writeTreeNode2Pack(zipOut, child));
        }
    }
}
