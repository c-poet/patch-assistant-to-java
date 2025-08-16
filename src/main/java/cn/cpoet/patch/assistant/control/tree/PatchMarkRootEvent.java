package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import javafx.event.Event;
import javafx.event.EventType;

/**
 * 标记根节点事件
 *
 * @author CPoet
 */
public class PatchMarkRootEvent extends Event {

    private static final long serialVersionUID = 5702388012006442736L;

    /**
     * 标记的根节点
     */
    private TreeNode treeNode;

    public PatchMarkRootEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }
}
