package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.control.tree.AppTreeInfo;
import cn.cpoet.patch.assistant.control.tree.TotalInfo;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.progress.ProgressContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
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
     * 应用信息
     */
    private final AppTreeInfo appTreeInfo;

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

    /**
     * 进度器
     */
    private final ProgressContext pc;

    public PatchMatchProcessor(TotalInfo totalInfo, AppTreeInfo appTreeInfo, boolean isWithPath, boolean isWithName, ProgressContext pc) {
        this.totalInfo = totalInfo;
        this.appTreeInfo = appTreeInfo;
        this.isWithPath = isWithPath;
        this.isWithName = isWithName;
        this.pc = pc;
    }

    public void setAppRootNode(TreeNode appRootNode) {
        this.appRootNode = appRootNode;
    }

    public void setPatchRootNode(TreeNode patchRootNode) {
        this.patchRootNode = patchRootNode;
    }

    private boolean checkRootNode() {
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
                    .filter(node -> TreeNodeType.NONE.equals(node.getType()))
                    .collect(Collectors.toMap(TreeNode::getName, Function.identity()));
            pc.step("number of nodes matched by name " + nameMapping.size());
        }
        match(appRootNode.getChildren(), isWithPath ? patchRootNode.getChildren() : null, nameMapping);
    }

    private <T> boolean hasMatch(List<T> list, Predicate<T> predicate) {
        boolean hasFlag = false;
        for (T item : list) {
            if (predicate.test(item)) {
                hasFlag = true;
            }
        }
        return hasFlag;
    }

    private boolean match(List<TreeNode> appNodes, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        return hasMatch(appNodes, appNode -> match(appNode, patchNodes, nameMapping));
    }

    private boolean match(TreeNode appNode, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        if (appNode.getMappedNode() != null) {
            return true;
        }
        if (CollectionUtil.isEmpty(patchNodes)) {
            return match0(appNode, null, nameMapping);
        }
        return hasMatch(patchNodes, patchNode -> appNode.getMappedNode() != null || match0(appNode, patchNode, nameMapping));
    }

    private boolean match0(TreeNode appNode, TreeNode patchNode, Map<String, TreeNode> nameMapping) {
        // 未满足路径匹配和名称匹配的条件时，直接返回结果
        if (patchNode == null && CollectionUtil.isEmpty(nameMapping)) {
            pc.step("not matched " + appNode.getPath());
            return false;
        }
        pc.step("matching " + appNode.getPath());
        boolean isMatch = patchNode != null && PatchPackService.INSTANCE.matchPatchName(appNode, patchNode);
        if (appNode.isDir()) {
            if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
                return match(appNode.getChildren(), isMatch ? patchNode.getChildren() : null, nameMapping);
            }
            return false;
        }
        if (appNode.getName().endsWith(FileExtConst.DOT_JAR)) {
            List<TreeNode> oldChildren = appNode.getChildren();
            if (oldChildren == null) {
                AppPackService.INSTANCE.buildChildrenWithCompress(appNode, false);
            }
            if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
                if (match(appNode.getChildren(), isMatch ? patchNode.getChildren() : null, nameMapping)) {
                    return true;
                }
            }
            appNode.setChildren(oldChildren);
        }
        if (isMatch) {
            pc.step("successful match " + appNode.getPath());
            TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.MOD);
            AppPackService.INSTANCE.createPatchDiffInfo(appTreeInfo, appNode, patchNode);
            PatchPackService.INSTANCE.mappedInnerClassNode(totalInfo, appTreeInfo, appNode, patchNode);
            return true;
        }
        return matchWithName(appNode, nameMapping);
    }

    private boolean matchWithName(TreeNode appNode, Map<String, TreeNode> nameMapping) {
        if (appNode.getMappedNode() != null) {
            return true;
        }
        if (!appNode.isDir()) {
            TreeNode patchNode = nameMapping.remove(appNode.getName());
            if (patchNode != null) {
                pc.step("successful match " + appNode.getPath());
                TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.MOD);
                AppPackService.INSTANCE.createPatchDiffInfo(appTreeInfo, appNode, patchNode);
                PatchPackService.INSTANCE.mappedInnerClassNode(totalInfo, appTreeInfo, appNode, patchNode);
                return true;
            }
        }
        return false;
    }
}
