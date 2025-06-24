package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.view.tree.*;
import javafx.event.EventType;

import java.util.Objects;

/**
 * 上下文
 *
 * @author CPoet
 */
public class HomeContext {

    protected final TotalInfo totalInfo = new TotalInfo();
    protected final AppTreeView appTree = new AppTreeView();
    protected final PatchTreeView patchTree = new PatchTreeView();

    public TotalInfo getTotalInfo() {
        return totalInfo;
    }

    public AppTreeView getAppTree() {
        return appTree;
    }

    public PatchTreeView getPatchTree() {
        return patchTree;
    }

    /**
     * 判断是否是补丁包手动标记的根节点
     *
     * @param node 节点
     * @return 是否是手动标记的根节点
     */
    public boolean isPatchCustomRoot(TreeNode node) {
        PatchTreeInfo treeInfo = patchTree.getTreeInfo();
        return treeInfo != null && Objects.equals(node, treeInfo.getCustomRootNode());
    }
}
