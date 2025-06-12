package cn.cpoet.patch.assistant.service;

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

    public PatchMatchProcessor(TotalInfo totalInfo, boolean isWithPath, boolean isWithName) {
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
        doExec(appRootNode.getChildren(), patchRootNode.getChildren(), nameMapping);
    }

    protected void doExec(List<TreeNode> appNodes, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        appNodes.forEach(appNode -> doExec(appNode, patchNodes, nameMapping));
    }

    protected void doExec(TreeNode appNode, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        if (appNode.getMappedNode() != null) {
            return;
        }
        if (isWithPath) {
            for (TreeNode patchNode : patchNodes) {
                if (appNode.getName().equals(patchNode.getName())) {
                    if (!appNode.isDir()) {
                        if (patchNode.getMappedNode() == null) {
                            TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeStatus.MOD);
                        }
                    } else {
                        if (CollectionUtil.isNotEmpty(appNode.getChildren()) && CollectionUtil.isNotEmpty(patchNode.getChildren())) {
                            doExec(appNode.getChildren(), patchNode.getChildren(), nameMapping);
                        }
                    }
                    break;
                }
            }
        }
        if (appNode.getMappedNode() != null || appNode.isDir()) {
            return;
        }
        TreeNode patchNode = nameMapping.remove(appNode.getName());
        if (patchNode != null) {
            TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeStatus.MOD);
        }
    }
}
