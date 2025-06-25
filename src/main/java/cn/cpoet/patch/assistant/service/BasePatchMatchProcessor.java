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
public abstract class BasePatchMatchProcessor {
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

    private final PatchPackService patchPackService;

    public BasePatchMatchProcessor(TotalInfo totalInfo, boolean isWithPath, boolean isWithName) {
        this.patchPackService = PatchPackService.getInstance();
        this.totalInfo = totalInfo;
        this.isWithPath = isWithPath;
        this.isWithName = isWithName;
    }

    public void exec() {
        List<TreeNode> patchNodes = getPatchNodes();
        Map<String, TreeNode> nameMapping = Collections.emptyMap();
        if (isWithName) {
            nameMapping = patchNodes.stream()
                    .filter(node -> !node.isDir() && node.getMappedNode() == null)
                    .collect(Collectors.toMap(TreeNode::getName, Function.identity()));
        }
        match(getAppNodes(), patchNodes, nameMapping);
    }

    protected abstract List<TreeNode> getAppNodes();

    protected abstract List<TreeNode> getPatchNodes();

    private boolean match(List<TreeNode> appNodes, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        return appNodes.stream().anyMatch(appNode -> match(appNode, patchNodes, nameMapping));
    }

    private boolean match(TreeNode appNode, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
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

    private boolean matchWithPath(TreeNode appNode, TreeNode patchNode, Map<String, TreeNode> nameMapping) {
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

    private boolean matchWithName(TreeNode appNode, Map<String, TreeNode> nameMapping) {
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
