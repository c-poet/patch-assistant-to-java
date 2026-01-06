package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;

import java.util.List;

/**
 * Cell拖动信息
 *
 * @author CPoet
 */
public class FileTreeCellDragInfo {

    /**
     * 拖动来源树
     */
    private CustomTreeView<?> originTree;

    /**
     * 存在readme节点
     */
    private boolean hasReadmeNode;

    /**
     * 存在已经绑定的节点
     */
    private boolean hasMappedNode;

    /**
     * 操作的节点列表
     */
    private List<TreeNode> treeNodes;


    public CustomTreeView<?> getOriginTree() {
        return originTree;
    }

    public void setOriginTree(CustomTreeView<?> originTree) {
        this.originTree = originTree;
    }

    public boolean isHasReadmeNode() {
        return hasReadmeNode;
    }

    public void setHasReadmeNode(boolean hasReadmeNode) {
        this.hasReadmeNode = hasReadmeNode;
    }

    public boolean isHasMappedNode() {
        return hasMappedNode;
    }

    public void setHasMappedNode(boolean hasMappedNode) {
        this.hasMappedNode = hasMappedNode;
    }

    public List<TreeNode> getTreeNodes() {
        return treeNodes;
    }

    public void setTreeNodes(List<TreeNode> treeNodes) {
        this.treeNodes = treeNodes;
    }
}
