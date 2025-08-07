package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.PatchConf;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.model.PatchSign;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.tree.*;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
     * @param rootNode 根节点信息
     */
    public void refreshReadmeNode(PatchTreeInfo patchTreeInfo, TreeNode rootNode) {
        PatchRootInfo rootInfo = patchTreeInfo.getRootInfoByNode(rootNode);
        if (rootInfo == null) {
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
        if (readmeNode != null) {
            readmeNode.setType(TreeNodeType.README);
            byte[] bytes = readmeNode.getBytes();
            if (bytes != null && bytes.length > 0) {
                rootInfo.getPatchSign().setReadme(new String(bytes));
            }
        }
    }

    /**
     * 清空节节点绑定的信息
     *
     * @param totalInfo     统计信息
     * @param rootNode      根节点
     * @param excludeReadme 是否排除Readme节点
     */
    public void cleanMappedNode(TotalInfo totalInfo, TreeNode rootNode, boolean excludeReadme) {
        cleanMappedNode(totalInfo, rootNode, excludeReadme, null);
    }

    /**
     * 清空节节点绑定的信息
     *
     * @param totalInfo     统计信息
     * @param rootNode      根节点
     * @param excludeReadme 是否排除Readme节点
     */
    public void cleanMappedNode(TotalInfo totalInfo, TreeNode rootNode, boolean excludeReadme, Predicate<TreeNode> filter) {
        if (excludeReadme) {
            TreeNodeUtil.deepCleanMappedNode(totalInfo, rootNode, node -> {
                if (Objects.equals(rootNode, node.getParent()) && TreeNodeType.README.equals(node.getType())) {
                    return true;
                }
                return filter != null && filter.test(node);
            });
        } else {
            TreeNodeUtil.deepCleanMappedNode(totalInfo, rootNode, filter);
        }
    }

    /**
     * 刷新补丁文件映射信息
     *
     * @param totalInfo     统计信息
     * @param appTreeInfo   应用树形信息
     * @param patchTreeInfo 补丁树形信息
     * @param rootNode      根节点
     */
    public void refreshMappedNode(TotalInfo totalInfo, AppTreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo, TreeNode rootNode) {
        cleanMappedNode(totalInfo, rootNode, true, node -> {
            if (node == rootNode) {
                return true;
            }
            if (TreeNodeType.CUSTOM_ROOT.equals(node.getType())) {
                patchTreeInfo.removeCustomRootInfo(node);
            }
            return false;
        });
        if (appTreeInfo == null || patchTreeInfo == null) {
            return;
        }
        refreshMappedNodeWithReadme(totalInfo, appTreeInfo, patchTreeInfo, rootNode);
        refreshMappedNodeWithPathOrName(totalInfo, appTreeInfo, patchTreeInfo);
    }

    private void refreshMappedNodeWithReadme(TotalInfo totalInfo, AppTreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo, TreeNode treeNode) {
        List<ReadMePathInfo> pathInfos = ReadMeFileService.getInstance().getPathInfos(patchTreeInfo, treeNode);
        if (CollectionUtil.isEmpty(pathInfos)) {
            return;
        }
        String pathPrefix = null;
        if ((TreeNodeType.CUSTOM_ROOT.equals(treeNode.getType()) && treeNode.isDir()) || (treeNode instanceof FileNode && treeNode.isDir())) {
            pathPrefix = treeNode.getPath();
        }
        for (ReadMePathInfo pathInfo : pathInfos) {
            String filePath = pathPrefix == null ? pathInfo.getFilePath() : FileNameUtil.joinPath(pathPrefix, pathInfo.getFilePath());
            TreeNode patchNode = TreeNodeUtil.findNodeByPath(treeNode, filePath);
            if (patchNode == null && !ReadMePathInfo.TypeEnum.DEL.equals(pathInfo.getType())) {
                continue;
            }
            matchMappedNodeWithReadme(totalInfo, pathInfo, appTreeInfo, patchNode);
        }
    }

    private void matchMappedNodeWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, AppTreeInfo appTreeInfo, TreeNode patchNode) {
        // 组装完成映射路径
        String appNodePath = pathInfo.getFirstPath();
        if (!StringUtil.isBlank(pathInfo.getSecondPath())) {
            appNodePath = FileNameUtil.joinPath(appNodePath, pathInfo.getSecondPath());
        }
        String[] paths = FileNameUtil.joinPath(appNodePath, pathInfo.getFilePath()).split(FileNameUtil.SEPARATOR);
        doMatchMappedNodeWithReadme(totalInfo, pathInfo, paths, 0, appTreeInfo.getRootNode().getChildren(), patchNode);
    }

    private boolean doMatchMappedNodeWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, String[] paths, int index, List<TreeNode> appNodes, TreeNode patchNode) {
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
                virtualTreeNode.setDir(true);
                virtualTreeNode.setModifyTime(now);
                newAppNode = virtualTreeNode;
            }
            newAppNode.setParent(parent);
            newAppNode.setPath(sb.append(paths[index]).toString());
            parent.getAndInitChildren().add(newAppNode);
            parent = newAppNode;
            ++index;
        }
        TreeNodeUtil.mappedNode(totalInfo, newAppNode, patchNode, TreeNodeType.ADD);
        addMappedNodeChildren(totalInfo, newAppNode, patchNode);
        return true;
    }

    private void addMappedNodeChildren(TotalInfo totalInfo, TreeNode appNode, TreeNode patchNode) {
        if (CollectionUtil.isEmpty(patchNode.getChildren())) {
            return;
        }
        Map<String, TreeNode> childAppNodeMap = Collections.emptyMap();
        if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
            childAppNodeMap = appNode.getChildren().stream().collect(Collectors.toMap(TreeNode::getName, Function.identity()));
        }
        for (TreeNode childPatchNode : patchNode.getChildren()) {
            TreeNode childAppNode = childAppNodeMap.get(childPatchNode.getName());
            if (childAppNode == null) {
                if (!childPatchNode.isDir()) {
                    childAppNode = new VirtualMappedNode(childPatchNode);
                } else {
                    VirtualTreeNode virtualTreeNode = new VirtualTreeNode();
                    virtualTreeNode.setName(childPatchNode.getName());
                    virtualTreeNode.setText(childPatchNode.getText());
                    virtualTreeNode.setDir(true);
                    virtualTreeNode.setModifyTime(childPatchNode.getModifyTime());
                    childAppNode = virtualTreeNode;
                }
                childAppNode.setParent(appNode);
                childAppNode.setPath(FileNameUtil.joinPath(appNode.getPath(), childAppNode.getName()));
                appNode.getAndInitChildren().add(childAppNode);
            }
            TreeNodeUtil.mappedNode(totalInfo, childAppNode, childPatchNode, TreeNodeType.ADD);
            addMappedNodeChildren(totalInfo, childAppNode, childPatchNode);
        }
    }

    private boolean doMatchMappedNodeWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, String[] paths, int index, TreeNode appNode, TreeNode patchNode) {
        if (!matchPatchName(appNode, paths[index])) {
            return false;
        }
        if (index == paths.length - 1) {
            if (ReadMePathInfo.TypeEnum.DEL.equals(pathInfo.getType())) {
                TreeNodeUtil.countAndSetNodeType(totalInfo, appNode, TreeNodeType.DEL);
            } else if (TreeNodeType.DEL.equals(appNode.getType())) {
                TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.ADD);
                if (ReadMePathInfo.TypeEnum.ADD.equals(pathInfo.getType())) {
                    addMappedNodeChildren(totalInfo, appNode, patchNode);
                }
            } else {
                TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.MOD);
                mappedInnerClassNode(totalInfo, appNode, patchNode);
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

    private void refreshMappedNodeWithPathOrName(TotalInfo totalInfo, AppTreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo) {
        PatchConf patch = Configuration.getInstance().getPatch();
        boolean isWithPath = Boolean.TRUE.equals(patch.getPathMatch());
        boolean isWithName = Boolean.TRUE.equals(patch.getFileNameMatch());
        if (isWithPath || isWithName) {
            PatchMatchProcessor processor = new PatchMatchProcessor(totalInfo, isWithPath, isWithName);
            processor.setAppRootNode(appTreeInfo.getRootNode());
            Map<TreeNode, PatchRootInfo> customRootInfoMap = patchTreeInfo.getCustomRootInfoMap();
            if (CollectionUtil.isNotEmpty(customRootInfoMap)) {
                for (Map.Entry<TreeNode, PatchRootInfo> entry : customRootInfoMap.entrySet()) {
                    processor.setPatchRootNode(entry.getKey());
                    processor.exec();
                }
            } else {
                processor.setPatchRootNode(patchTreeInfo.getRootNode());
                processor.exec();
            }
        }
    }

    /**
     * 绑定内部类
     *
     * @param totalInfo 统计信息
     * @param appNode   应用节点
     * @param patchNode 补丁节点
     */
    public void mappedInnerClassNode(TotalInfo totalInfo, TreeNode appNode, TreeNode patchNode) {
        if (!appNode.getName().endsWith(FileExtConst.DOT_CLASS) || !patchNode.getName().endsWith(FileExtConst.DOT_CLASS)) {
            return;
        }
        if (CollectionUtil.isEmpty(appNode.getChildren()) && CollectionUtil.isEmpty(patchNode.getChildren())) {
            return;
        }
        Map<String, TreeNode> innerNodeMap = appNode
                .getChildren()
                .stream()
                .collect(Collectors.toMap(TreeNode::getName, Function.identity()));
        for (TreeNode patchInnerNode : patchNode.getChildren()) {
            TreeNode appInnerNode = innerNodeMap.get(patchInnerNode.getName());
            if (appInnerNode == null) {
                appInnerNode = new VirtualMappedNode(patchInnerNode);
                appInnerNode.setParent(appNode);
                appInnerNode.setPath(FileNameUtil.joinPath(FileNameUtil.getDirPath(appNode.getPath()), patchInnerNode.getName()));
                appNode.getAndInitChildren().add(appInnerNode);
                TreeNodeUtil.mappedNode(totalInfo, appInnerNode, patchInnerNode, TreeNodeType.ADD);
                continue;
            }
            TreeNodeUtil.mappedNode(totalInfo, appInnerNode, patchInnerNode, TreeNodeType.MOD);
            mappedInnerClassNode(totalInfo, appInnerNode, patchInnerNode);
            innerNodeMap.remove(patchInnerNode.getName());
        }
        if (CollectionUtil.isNotEmpty(innerNodeMap)) {
            innerNodeMap.forEach((k, v) -> TreeNodeUtil.countAndSetNodeType(totalInfo, v, TreeNodeType.DEL));
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
        rootNode.setType(TreeNodeType.ROOT);
        PatchSign patchSign = new PatchSign();
        patchSign.setName(rootNode.getName());
        if (file.isDirectory()) {
            doGetTreeNodeWithDir(file, rootNode);
        } else {
            doGetTreeNodeWithZip(patchSign, file, rootNode);
        }
        treeInfo.setRootNode(rootNode);
        PatchRootInfo patchRootInfo = createPatchRootInfo(rootNode);
        treeInfo.setRootInfo(patchRootInfo);
        return treeInfo;
    }

    private void doGetTreeNodeWithDir(File file, TreeNode parentNode) {
        File[] files = file.listFiles();
        if (files != null) {
            List<TreeNode> innerClasses = null;
            for (File childFile : files) {
                FileNode fileNode = new FileNode();
                fileNode.setName(childFile.getName());
                fileNode.setText(childFile.getName());
                fileNode.setPath(childFile.getPath());
                fileNode.setFile(childFile);
                fileNode.setParent(parentNode);
                fileNode.setPatch(true);
                if (childFile.isDirectory()) {
                    doGetTreeNodeWithDir(childFile, fileNode);
                    parentNode.getAndInitChildren().add(fileNode);
                } else if (childFile.getName().endsWith(FileExtConst.DOT_CLASS) && childFile.getName().indexOf('$') != -1) {
                    if (innerClasses == null) {
                        innerClasses = new ArrayList<>();
                    }
                    innerClasses.add(fileNode);
                } else {
                    parentNode.getAndInitChildren().add(fileNode);
                }
            }
            if (CollectionUtil.isNotEmpty(innerClasses)) {
                handleInnerClass(parentNode, innerClasses);
            }
        }
    }

    private void doGetTreeNodeWithZip(PatchSign patchSign, File file, TreeNode rootNode) {
        byte[] bytes;
        try (InputStream in = new FileInputStream(file)) {
            bytes = in.readAllBytes();
        } catch (IOException ex) {
            throw new AppException("读取补丁压缩包内容失败", ex);
        }
        rootNode.setSize(bytes.length);
        patchSign.setMd5(HashUtil.md5(bytes));
        rootNode.setMd5(patchSign.getMd5());
        patchSign.setSha1(HashUtil.sha1(bytes));
        doGetTreeNodeWithZip(new ByteArrayInputStream(bytes), rootNode);
    }

    private void doGetTreeNodeWithZip(InputStream in, TreeNode rootNode) {
        try (ZipInputStream zin = new ZipInputStream(in, Charset.forName("GBK"))) {
            doReadZipEntry(rootNode, zin, true);
        } catch (IOException ex) {
            throw new AppException("读取补丁压缩包失败", ex);
        }
    }

    /**
     * 新增补丁信息
     *
     * @param treeNode 节点
     * @return 包装后的{@link PatchSignTreeNode}节点
     */
    public PatchRootInfo createPatchRootInfo(TreeNode treeNode) {
        PatchSign patchSign = new PatchSign();
        patchSign.setName(treeNode.getName());
        if (!treeNode.isDir()) {
            patchSign.setMd5(treeNode.getMd5());
            patchSign.setSha1(HashUtil.sha1(treeNode.getBytes()));
        }
        PatchRootInfo patchRootInfo = new PatchRootInfo();
        patchRootInfo.setPatchSign(patchSign);
        return patchRootInfo;
    }
}
