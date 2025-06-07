package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.tree.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * 补丁包处理
 *
 * @author CPoet
 */
public class PatchPackService extends BasePackService {

    public static PatchPackService getInstance() {
        return AppContext.getInstance().getService(PatchPackService.class);
    }

    /**
     * 更新根节点信息
     *
     * @param patchTreeInfo 树形信息
     */
    public void refreshReadmeNode(PatchTreeInfo patchTreeInfo) {
        if (patchTreeInfo == null) {
            return;
        }
        TreeNode rootNode = patchTreeInfo.getCustomRootNode() != null ? patchTreeInfo.getCustomRootNode() : patchTreeInfo.getRootNode();
        if (CollectionUtil.isEmpty(rootNode.getChildren())) {
            return;
        }
        String readmeFileName = Configuration.getInstance().getPatch().getReadmeFile();
        if (StringUtil.isBlank(readmeFileName)) {
            readmeFileName = AppConst.README_FILE;
        }
        TreeNode readmeNode = null;
        for (TreeNode child : rootNode.getChildren()) {
            if (readmeFileName.equalsIgnoreCase(child.getText())) {
                readmeNode = child;
                break;
            }
        }
        patchTreeInfo.setReadMeNode(readmeNode);
    }

    /**
     * 清理映射的节点信息
     *
     * @param node 节点信息
     */
    public void cleanMappedNode(TreeNode node) {
        node.setMappedNode(null);
        if (node.getChildren() != null) {
            node.getChildren().forEach(this::cleanMappedNode);
        }
    }

    /**
     * 刷新补丁文件映射信息
     *
     * @param totalInfo     统计信息
     * @param appTreeInfo   应用树形信息
     * @param patchTreeInfo 补丁树形信息
     */
    public void refreshPatchMappedNode(TotalInfo totalInfo, TreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo) {
        if (appTreeInfo != null) {
            cleanMappedNode(appTreeInfo.getRootNode());
        }
        if (patchTreeInfo != null) {
            cleanMappedNode(patchTreeInfo.getRootNode());
        }
        totalInfo.rest();
        if (appTreeInfo == null || patchTreeInfo == null) {
            return;
        }
        doPatchMappedNodeWithReadme(totalInfo, appTreeInfo, patchTreeInfo);
        doPatchMappedNodeWithPath(totalInfo, appTreeInfo, patchTreeInfo);
    }

    protected void doPatchMappedNodeWithReadme(TotalInfo totalInfo, TreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo) {
        List<ReadMePathInfo> pathInfos = ReadMeFileService.getInstance().getPathInfos(patchTreeInfo);
        if (pathInfos == null || pathInfos.isEmpty()) {
            return;
        }
        String pathPrefix = null;
        TreeNode rootNode = patchTreeInfo.getRootNode();
        if (patchTreeInfo.getCustomRootNode() != null) {
            rootNode = patchTreeInfo.getCustomRootNode();
            pathPrefix = rootNode instanceof TreeKindNode ? ((TreeKindNode) rootNode).getPath() : null;
        } else if (rootNode instanceof FileNode && ((FileNode) rootNode).isDir()) {
            pathPrefix = ((FileNode) rootNode).getPath() + FileNameUtil.SEPARATOR;
        }
        for (ReadMePathInfo pathInfo : pathInfos) {
            String fileName = pathInfo.getFileName();
            String firstPath = pathInfo.getFirstPath();
            String secondPath = pathInfo.getSecondPath();
            TreeNode patchNode = TreeNodeUtil.findNodeByPath(rootNode, pathPrefix == null ? fileName : pathPrefix + fileName);
            if (patchNode == null) {
                continue;
            }
            TreeNode firstNode = null;
            String dirPath = FileNameUtil.getDirPath(firstPath) + FileNameUtil.SEPARATOR;
            TreeNode dirNode = TreeNodeUtil.findNodeByPath(appTreeInfo.getRootNode(), dirPath);
            if (dirNode != null && dirNode.getChildren() != null && !dirNode.getChildren().isEmpty()) {
                for (TreeNode child : dirNode.getChildren()) {
                    if (((TreeKindNode) child).getPath().startsWith(firstPath)) {
                        firstNode = child;
                        break;
                    }
                }
            }
            TreeNode secondNode = null;
            if (firstNode != null) {
                if (firstNode.getText().endsWith(FileExtConst.DOT_JAR)) {
                    if (buildNodeChildrenWithZip(firstNode, false)) {
                        TreeNodeUtil.buildNodeChildren(firstNode.getTreeItem(), firstNode, OnlyChangeFilter.INSTANCE);
                    }
                }
                secondNode = TreeNodeUtil.findNodeByPath(firstNode, secondPath + FileNameUtil.SEPARATOR);
            }
            if (secondNode != null && secondNode.getChildren() != null && !secondNode.getChildren().isEmpty()) {
                for (TreeNode appNode : secondNode.getChildren()) {
                    if (fileName.equals(appNode.getText())) {
                        patchNode.setMappedNode(appNode);
                        appNode.setMappedNode(patchNode);
                        // BY CPoet 后续处理删除和新增的情况
                        totalInfo.incrTotal(TreeNodeStatus.MOD);
                        break;
                    }
                }
            }
        }
    }

    protected void doPatchMappedNodeWithPath(TotalInfo totalInfo, TreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo) {

    }

    /**
     * 解析树形节点
     *
     * @param file 文件
     * @return 树形信息
     */
    public PatchTreeInfo getTreeNode(File file) {
        PatchTreeInfo treeInfo = new PatchTreeInfo();
        FileNode rootNode = new FileNode();
        rootNode.setName(file.getName());
        rootNode.setText(file.getName());
        rootNode.setPath(file.getPath());
        rootNode.setFile(file);
        rootNode.setPatch(true);
        treeInfo.setRootNode(rootNode);
        if (file.isDirectory()) {
            doGetTreeNodeWithDir(file, rootNode);
        } else {
            doGetTreeNodeWithZip(file, rootNode);
        }
        return treeInfo;
    }

    protected void doGetTreeNodeWithDir(File file, TreeKindNode parentNode) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File childFile : files) {
                FileNode fileNode = new FileNode();
                fileNode.setName(childFile.getName());
                fileNode.setText(childFile.getName());
                fileNode.setPath(childFile.getPath());
                fileNode.setFile(childFile);
                fileNode.setParent(parentNode);
                fileNode.setPatch(true);
                parentNode.getAndInitChildren().add(fileNode);
                if (childFile.isDirectory()) {
                    doGetTreeNodeWithDir(childFile, fileNode);
                }
            }
        }
    }

    protected void doGetTreeNodeWithZip(File file, TreeKindNode rootNode) {
        try (InputStream in = new FileInputStream(file);
             ZipInputStream zin = new ZipInputStream(in, Charset.forName("GBK"))) {
            doReadZipEntry(rootNode, zin, true);
        } catch (IOException ex) {
            throw new AppException("写入文件到压缩包失败", ex);
        }
    }
}
