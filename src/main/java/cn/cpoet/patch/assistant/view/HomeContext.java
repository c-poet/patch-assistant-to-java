package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.view.tree.PatchTreeInfo;
import cn.cpoet.patch.assistant.view.tree.TotalInfo;
import cn.cpoet.patch.assistant.view.tree.TreeInfo;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.event.EventType;
import javafx.scene.control.TreeView;

import java.util.Objects;

/**
 * 上下文
 *
 * @author CPoet
 */
public class HomeContext {

    public static final EventType<?> APP_TREE_REFRESH = new EventType<>("APP_TREE_REFRESH");
    public static final EventType<?> PATCH_TREE_REFRESH = new EventType<>("PATCH_TREE_REFRESH");

    protected TreeInfo appTreeInfo;
    protected PatchTreeInfo patchTreeInfo;
    protected TreeView<TreeNode> appTree = new TreeView<>();
    protected TreeView<TreeNode> patchTree = new TreeView<>();
    protected final TotalInfo totalInfo = new TotalInfo();

    public TotalInfo getTotalInfo() {
        return totalInfo;
    }

    public TreeInfo getAppTreeInfo() {
        return appTreeInfo;
    }

    public TreeView<TreeNode> getAppTree() {
        return appTree;
    }

    public PatchTreeInfo getPatchTreeInfo() {
        return patchTreeInfo;
    }

    public TreeView<TreeNode> getPatchTree() {
        return patchTree;
    }

    /**
     * 判断是否是补丁包手动标记的根节点
     *
     * @param node 节点
     * @return 是否是手动标记的根节点
     */
    public boolean isPatchCustomRoot(TreeNode node) {
        return patchTreeInfo != null && Objects.equals(node, patchTreeInfo.getCustomRootNode());
    }
}
