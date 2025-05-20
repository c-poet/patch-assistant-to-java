package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.view.tree.FileNode;
import cn.cpoet.patch.assistant.view.tree.TreeInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        treeInfo.setRootNode(rootNode);
        try (InputStream in = new FileInputStream(file);
             ZipInputStream zin = new ZipInputStream(in)) {
            doReadZipEntry(rootNode, zin);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return treeInfo;
    }
}
