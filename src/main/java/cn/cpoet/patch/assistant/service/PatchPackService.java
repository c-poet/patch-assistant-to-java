package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.PatchConf;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
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
     * 刷新补丁文件映射信息
     *
     * @param totalInfo     统计信息
     * @param appTreeInfo   应用树形信息
     * @param patchTreeInfo 补丁树形信息
     */
    public void refreshMappedNode(TotalInfo totalInfo, TreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo) {
        if (appTreeInfo != null) {
            TreeNodeUtil.cleanMappedNode(appTreeInfo.getRootNode());
        }
        if (patchTreeInfo != null) {
            TreeNodeUtil.cleanMappedNode(patchTreeInfo.getRootNode());
        }
        totalInfo.rest();
        if (appTreeInfo == null || patchTreeInfo == null) {
            return;
        }
        refreshMappedNodeWithReadme(totalInfo, appTreeInfo, patchTreeInfo);
        refreshMappedNodeWithPathOrName(totalInfo, appTreeInfo, patchTreeInfo);
    }

    protected void refreshMappedNodeWithReadme(TotalInfo totalInfo, TreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo) {
        List<ReadMePathInfo> pathInfos = ReadMeFileService.getInstance().getPathInfos(patchTreeInfo);
        if (CollectionUtil.isEmpty(pathInfos)) {
            return;
        }
        String pathPrefix = null;
        TreeNode rootNode = patchTreeInfo.getCurRootNode();
        if (patchTreeInfo.getCustomRootNode() != null || (rootNode instanceof FileNode && rootNode.isDir())) {
            pathPrefix = rootNode.getPath();
        }
        for (ReadMePathInfo pathInfo : pathInfos) {
            String filePath = pathPrefix == null ? pathInfo.getFilePath() : FileNameUtil.joinPath(pathPrefix, pathInfo.getFilePath());
            TreeNode patchNode = TreeNodeUtil.findNodeByPath(rootNode, filePath);
            if (patchNode == null && !ReadMePathInfo.TypeEnum.DEL.equals(pathInfo.getType())) {
                continue;
            }
            matchMappedNodeWithReadme(totalInfo, pathInfo, appTreeInfo, patchNode);
        }
    }

    protected void matchMappedNodeWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, TreeInfo appTreeInfo, TreeNode patchNode) {
        // 组装完成映射路径
        String appNodePath = pathInfo.getFirstPath();
        if (!StringUtil.isBlank(pathInfo.getSecondPath())) {
            appNodePath = FileNameUtil.joinPath(appNodePath, pathInfo.getSecondPath());
        }
        String[] paths = FileNameUtil.joinPath(appNodePath, pathInfo.getFilePath()).split(FileNameUtil.SEPARATOR);
        doMatchMappedNodeWithReadme(totalInfo, pathInfo, paths, 0, appTreeInfo.getRootNode().getChildren(), patchNode);
    }

    protected boolean doMatchMappedNodeWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, String[] paths, int index, List<TreeNode> appNodes, TreeNode patchNode) {
        if (index >= paths.length) {
            return false;
        }
        for (TreeNode appNode : appNodes) {
            if (doMatchMappedNodeWithReadme(totalInfo, pathInfo, paths, index, appNode, patchNode)) {
                return true;
            }
        }
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder(paths.length);
        for (int i = 0; i < index; ++i) {
            sb.append(paths[i]);
        }
        if (!ReadMePathInfo.TypeEnum.ADD.equals(pathInfo.getType())) {
            return false;
        }
        TreeNode parent = appNodes.get(0).getParent();
        TreeNode newAppNode = null;
        while (index < paths.length) {
            if (index + 1 == paths.length) {
                newAppNode = new VirtualMappedNode(patchNode);
            } else {
                VirtualTreeNode virtualTreeNode = new VirtualTreeNode();
                virtualTreeNode.setName(paths[index]);
                virtualTreeNode.setText(paths[index]);
                virtualTreeNode.setPath(sb.append(paths[index]).toString());
                virtualTreeNode.setDir(index + 1 < paths.length);
                virtualTreeNode.setModifyTime(now);
                newAppNode = virtualTreeNode;
            }
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(newAppNode);
            parent = newAppNode;
            ++index;
        }
        TreeNodeUtil.mappedNode(totalInfo, newAppNode, patchNode, TreeNodeStatus.ADD);
        return true;
    }

    protected boolean doMatchMappedNodeWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, String[] paths, int index, TreeNode appNode, TreeNode patchNode) {
        if (!matchPatchName(appNode, paths[index])) {
            return false;
        }
        if (index == paths.length - 1) {
            if (ReadMePathInfo.TypeEnum.DEL.equals(pathInfo.getType())) {
                appNode.setStatus(TreeNodeStatus.DEL);
                totalInfo.incrTotal(TreeNodeStatus.DEL);
            } else {
                TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeStatus.MOD);
            }
            return true;
        }
        if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
            return doMatchMappedNodeWithReadme(totalInfo, pathInfo, paths, ++index, appNode.getChildren(), patchNode);
        }
        if (appNode.getName().endsWith(FileExtConst.DOT_JAR) && buildNodeChildrenWithZip(appNode, false)) {
            return doMatchMappedNodeWithReadme(totalInfo, pathInfo, paths, ++index, appNode.getChildren(), patchNode);
        }
        return false;
    }

    protected void refreshMappedNodeWithPathOrName(TotalInfo totalInfo, TreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo) {
        PatchConf patch = Configuration.getInstance().getPatch();
        boolean isWithPath = Boolean.TRUE.equals(patch.getPathMatch());
        boolean isWithName = Boolean.TRUE.equals(patch.getFileNameMatch());
        if (isWithPath || isWithName) {
            PatchMatchProcessor processor = new PatchMatchProcessor(this, totalInfo, isWithPath, isWithName);
            processor.setAppRootNode(appTreeInfo.getRootNode());
            processor.setPatchRootNode(patchTreeInfo.getCurRootNode());
            processor.exec();
        }
    }

    /**
     * 匹配名称
     * <p>Jar包采用前缀匹配的方式</p>
     *
     * @param appNode   应用节点
     * @param patchNode 补丁节点
     * @return 是否匹配
     */
    public boolean matchPatchName(TreeNode appNode, TreeNode patchNode) {
        return matchPatchName(appNode, patchNode.getName());
    }


    /**
     * 匹配名称
     * <p>Jar包采用前缀匹配的方式</p>
     *
     * @param appNode   应用节点
     * @param patchName 补丁节点名称
     * @return 是否匹配
     */
    public boolean matchPatchName(TreeNode appNode, String patchName) {
        if (appNode.getName().endsWith(FileExtConst.DOT_JAR)) {
            return appNode.getName().startsWith(patchName);
        }
        return appNode.getName().equals(patchName);
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

    protected void doGetTreeNodeWithDir(File file, TreeNode parentNode) {
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

    protected void doGetTreeNodeWithZip(File file, TreeNode rootNode) {
        try (InputStream in = new FileInputStream(file);
             ZipInputStream zin = new ZipInputStream(in, Charset.forName("GBK"))) {
            doReadZipEntry(rootNode, zin, true);
        } catch (IOException ex) {
            throw new AppException("写入文件到压缩包失败", ex);
        }
    }
}
