package cn.cpoet.patch.assistant.view.tree;

/**
 * 树形信息
 *
 * @author CPoet
 */
public abstract class TreeInfo<T extends TreeNode> {
    /**
     * 根节点信息
     */
    private T rootNode;

    public T getRootNode() {
        return rootNode;
    }

    public void setRootNode(T rootNode) {
        this.rootNode = rootNode;
    }
}
