package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.core.SearchItem;
import cn.cpoet.patch.assistant.view.tree.TreeNode;

/**
 * @author CPoet
 */
public class SearchNodeItem extends SearchItem {
    /**
     * 对应的节点
     */
    private transient TreeNode node;

    public TreeNode getNode() {
        return node;
    }

    public void setNode(TreeNode node) {
        this.node = node;
    }
}
