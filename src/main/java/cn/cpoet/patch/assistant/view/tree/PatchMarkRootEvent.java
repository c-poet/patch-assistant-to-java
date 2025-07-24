package cn.cpoet.patch.assistant.view.tree;

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
     * 是否新增标记根节点
     */
    private boolean isAdd;

    /**
     * 标记的根节点
     */
    private PatchSignTreeNode treeNode;

    public PatchMarkRootEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
    }

    public PatchSignTreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(PatchSignTreeNode treeNode) {
        this.treeNode = treeNode;
    }
}
