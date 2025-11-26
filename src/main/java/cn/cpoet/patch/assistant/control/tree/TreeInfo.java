package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;

/**
 * 树形信息
 *
 * @author CPoet
 */
public abstract class TreeInfo {
    /**
     * 根节点信息
     */
    private TreeNode rootNode;

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }
}
