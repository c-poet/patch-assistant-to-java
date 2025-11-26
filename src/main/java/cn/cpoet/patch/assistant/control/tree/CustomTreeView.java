package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * @author CPoet
 */
public abstract class CustomTreeView<TI extends TreeInfo> extends TreeView<TreeNode> {
    /**
     * 节点信息
     */
    private final ObjectProperty<TI> treeInfo = new SimpleObjectProperty<>();

    public CustomTreeView() {
        super();
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public TI getTreeInfo() {
        return treeInfo.get();
    }

    public ObjectProperty<TI> treeInfoProperty() {
        return treeInfo;
    }

    public void setTreeInfo(TI treeInfo) {
        this.treeInfo.set(treeInfo);
    }

    public boolean isSingleSelectedNode() {
        return getSingleSelectedNode() != null;
    }

    public TreeNode getSingleSelectedNode() {
        ObservableList<TreeItem<TreeNode>> selectedItems = getSelectionModel().getSelectedItems();
        if (selectedItems.size() != 1) {
            return null;
        }
        return selectedItems.get(0).getValue();
    }
}
