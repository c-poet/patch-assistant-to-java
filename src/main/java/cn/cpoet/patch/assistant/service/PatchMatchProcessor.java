package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.tree.TotalInfo;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.TreeNodeStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 补丁匹配处理器
 *
 * @author CPoet
 */
public class PatchMatchProcessor {
    /**
     * 统计信息
     */
    private final TotalInfo totalInfo;

    /**
     * 路径匹配
     */
    private final boolean isWithPath;

    /**
     * 名称匹配
     */
    private final boolean isWithName;

    /**
     * 应用包根节点
     */
    private TreeNode appRootNode;

    /**
     * 补丁包根节点
     */
    private TreeNode patchRootNode;

    private final PatchPackService patchPackService;

    public PatchMatchProcessor(PatchPackService patchPackService, TotalInfo totalInfo, boolean isWithPath, boolean isWithName) {
        this.patchPackService = patchPackService;
        this.totalInfo = totalInfo;
        this.isWithPath = isWithPath;
        this.isWithName = isWithName;
    }

    public void setAppRootNode(TreeNode appRootNode) {
        this.appRootNode = appRootNode;
    }

    public void setPatchRootNode(TreeNode patchRootNode) {
        this.patchRootNode = patchRootNode;
    }

    protected boolean checkRootNode() {
        return appRootNode != null && CollectionUtil.isNotEmpty(appRootNode.getChildren())
                && patchRootNode != null && CollectionUtil.isNotEmpty(patchRootNode.getChildren());
    }

    public void exec() {
        if (!checkRootNode()) {
            return;
        }
        Map<String, TreeNode> nameMapping = Collections.emptyMap();
        if (isWithName) {
            nameMapping = patchRootNode.getChildren().stream()
                    .filter(node -> !node.isDir() && node.getMappedNode() == null)
                    .collect(Collectors.toMap(TreeNode::getName, Function.identity()));
        }
        match(appRootNode.getChildren(), patchRootNode.getChildren(), nameMapping);
    }

    protected boolean match(List<TreeNode> appNodes, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        return appNodes.stream().anyMatch(appNode -> match(appNode, patchNodes, nameMapping));
    }

    protected boolean match(TreeNode appNode, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        if (appNode.getMappedNode() != null) {
            return false;
        }
        if (isWithPath) {
            for (TreeNode patchNode : patchNodes) {
                if (patchNode.getMappedNode() != null) {
                    continue;
                }
                if (matchWithPath(appNode, patchNode, nameMapping)) {
                    return true;
                }
            }
        }
        return matchWithName(appNode, nameMapping);
    }

    protected boolean matchWithPath(TreeNode appNode, TreeNode patchNode, Map<String, TreeNode> nameMapping) {
        if (!patchPackService.matchPatchName(appNode, patchNode)) {
            return false;
        }
        if (appNode.isDir()) {
            if (CollectionUtil.isNotEmpty(appNode.getChildren()) && CollectionUtil.isNotEmpty(patchNode.getChildren())) {
                return match(appNode.getChildren(), patchNode.getChildren(), nameMapping);
            }
            return false;
        }
        if (appNode.getName().endsWith(FileExtConst.DOT_JAR) && !CollectionUtil.isEmpty(patchNode.getChildren())) {
            List<TreeNode> oldChildren = appNode.getChildren();
            if (CollectionUtil.isEmpty(oldChildren)) {
                appNode.setChildren(null);
                if (patchPackService.buildNodeChildrenWithZip(appNode, false)
                        && match(appNode.getChildren(), patchNode.getChildren(), nameMapping)) {
                    return true;
                }
            }
            // 没匹配成功的情况下还原旧的值
            appNode.setChildren(oldChildren);
        }
        TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeStatus.MOD);
        patchPackService.mappedInnerClassNode(totalInfo, appNode, patchNode);
        return true;
    }

    protected boolean matchWithName(TreeNode appNode, Map<String, TreeNode> nameMapping) {
        if (appNode.getMappedNode() != null || appNode.isDir()) {
            return false;
        }
        TreeNode patchNode = nameMapping.remove(appNode.getName());
        if (patchNode != null) {
            TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeStatus.MOD);
            patchPackService.mappedInnerClassNode(totalInfo, appNode, patchNode);
            return true;
        }
        return false;
    }
}
