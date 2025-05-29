package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

/**
 * @author CPoet
 */
public abstract class HomeTreeView {

    protected final Stage stage;
    protected final HomeContext context;

    protected HomeTreeView(Stage stage, HomeContext context) {
        this.stage = stage;
        this.context = context;
    }

    protected void selectedLink(TreeView<TreeNode> originTree, TreeView<TreeNode> targetTree) {
        TreeItem<TreeNode> originItem = originTree.getSelectionModel().getSelectedItem();
        if (originItem == null) {
            return;
        }
        TreeNode appNode = originItem.getValue();
        if (appNode.getMappedNode() == null) {
            return;
        }
        TreeItem<TreeNode> targetItem = appNode.getMappedNode().getTreeItem();
        targetTree.getSelectionModel().select(targetItem);
        int targetItemIndex = targetTree.getRow(targetItem);
        targetTree.scrollTo(targetItemIndex);
    }
}
