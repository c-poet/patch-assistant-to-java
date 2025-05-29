package cn.cpoet.patch.assistant.view.tree;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形节点
 *
 * @author CPoet
 */
public class TreeNode {

    /**
     * 节点显示文本
     */
    protected String text;

    /**
     * 节点名称
     */
    protected String name;

    /**
     * 映射的节点信息
     */
    protected TreeNode mappedNode;

    /**
     * 是否选中
     */
    protected boolean checked = true;

    /**
     * 是否补丁
     */
    protected boolean isPatch = false;

    /**
     * 父级节点
     */
    protected TreeNode parent;

    /**
     * 子级节点列表
     */
    protected List<TreeNode> children;

    /**
     * 对应的节点项
     */
    protected TreeItem<TreeNode> treeItem;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isPatch() {
        return isPatch;
    }

    public void setPatch(boolean patch) {
        isPatch = patch;
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

    public List<TreeNode> getAndInitChildren() {
        return children == null ? (children = new ArrayList<>()) : children;
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
