package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.jdk.SortLinkedList;
import cn.cpoet.patch.assistant.util.HashUtil;
import javafx.scene.control.TreeItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 树形节点
 *
 * @author CPoet
 */
public abstract class TreeNode {

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


    /**
     * 路径
     */
    protected String path;

    /**
     * 内容
     */
    protected byte[] bytes;

    /**
     * 内容md5值
     */
    protected String md5;

    /**
     * 内容大小
     */
    protected long size;

    /**
     * 节点是否展开，用以保留展开状态
     */
    protected boolean expanded;

    /**
     * 节点状态
     */
    protected TreeNodeStatus status = TreeNodeStatus.NONE;

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
        return children == null ? (children = new SortLinkedList<>((o1, o2) -> o1.text.compareToIgnoreCase(o2.text))) : children;
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


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getMd5() {
        if (md5 != null) {
            return md5;
        }
        byte[] data = getBytes();
        return data == null ? "" : (md5 = HashUtil.md5(data));
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSize() {
        if (size != 0 && size != -1) {
            return size;
        }
        return (size = getBytes().length);
    }

    public void setSize(long size) {
        this.size = size;
    }

    /**
     * 判断当前节点是否目录
     *
     * @return 是否目录
     */
    public abstract boolean isDir();

    /**
     * 获取当前节点的更新时间
     *
     * @return 当前节点的更新时间
     */
    public abstract LocalDateTime getModifyTime();

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public TreeNodeStatus getStatus() {
        return status;
    }

    public void setStatus(TreeNodeStatus status) {
        if (!TreeNodeStatus.NONE.equals(status) && parent != null && TreeNodeStatus.NONE.equals(parent.getStatus())) {
            parent.setStatus(TreeNodeStatus.CHANGE);
        }
        this.status = status;
    }
}
