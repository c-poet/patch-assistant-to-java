package cn.cpoet.patch.assistant.view.tree;

import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * 树形节点
 *
 * @author CPoet
 */
public class TreeNode {

    /**
     * 节点名称
     */
    private String name;

    /**
     * 映射的节点信息
     */
    private TreeNode mappedNode;

    /**
     * 是否选中
     */
    private Boolean checked = Boolean.TRUE;

    /**
     * 父级节点
     */
    private TreeNode parent;

    /**
     * 子级节点列表
     */
    private List<TreeNode> children;

    /**
     * 对应的节点项
     */
    private TreeItem<TreeNode> treeItem;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public TreeNode getMappedNode() {
        return mappedNode;
    }

    public void setMappedNode(TreeNode mappedNode) {
        this.mappedNode = mappedNode;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public TreeItem<TreeNode> getTreeItem() {
        return treeItem;
    }

    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
    }
}
