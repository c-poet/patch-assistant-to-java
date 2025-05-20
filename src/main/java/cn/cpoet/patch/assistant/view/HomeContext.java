package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.view.tree.PatchTreeInfo;
import cn.cpoet.patch.assistant.view.tree.TreeInfo;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;

import java.util.Objects;

/**
 * 上下文
 *
 * @author CPoet
 */
public class HomeContext {

    protected TreeInfo appTreeInfo;
    protected TextField appPathTextField;
    protected TreeView<TreeNode> appTree;
    protected PatchTreeInfo patchTreeInfo;
    protected TreeView<TreeNode> patchTree;
    protected TextField patchPathTextField;
    protected TextArea readMeTextArea;

    public TreeInfo getAppTreeInfo() {
        return appTreeInfo;
    }

    public TextArea getReadMeTextArea() {
        return readMeTextArea;
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

    public TextField getAppPathTextField() {
        return appPathTextField;
    }

    public TextField getPatchPathTextField() {
        return patchPathTextField;
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
