package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.model.PatchSign;
import javafx.scene.control.TreeItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 补丁签名节点
 * <p>主要应用于根节点和标记根节点</p>
 *
 * @author CPoet
 */
public class PatchSignTreeNode extends TreeNode {

    private TreeNode readmeNode;
    private final TreeNode originNode;
    private final PatchSign patchSign;

    public PatchSignTreeNode(TreeNode originNode, PatchSign patchSign) {
        this.originNode = originNode;
        this.patchSign = patchSign;
    }

    public TreeNode getReadmeNode() {
        return readmeNode;
    }

    public void setReadmeNode(TreeNode readmeNode) {
        this.readmeNode = readmeNode;
    }

    public TreeNode getOriginNode() {
        return originNode;
    }

    public PatchSign getPatchSign() {
        return patchSign;
    }

    @Override
    public boolean isDir() {
        return originNode.isDir();
    }

    @Override
    public LocalDateTime getModifyTime() {
        return originNode.getModifyTime();
    }

    @Override
    public String getText() {
        return originNode.getText();
    }

    @Override
    public void setText(String text) {
        originNode.setText(text);
    }

    @Override
    public String getName() {
        return originNode.getName();
    }

    @Override
    public void setName(String name) {
        originNode.setName(name);
    }

    @Override
    public boolean isPatch() {
        return originNode.isPatch();
    }

    @Override
    public void setPatch(boolean patch) {
        originNode.setPatch(patch);
    }

    @Override
    public TreeNode getMappedNode() {
        return originNode.getMappedNode();
    }

    @Override
    public void setMappedNode(TreeNode mappedNode) {
        originNode.setMappedNode(mappedNode);
    }

    @Override
    public TreeNode getParent() {
        return originNode.getParent();
    }

    @Override
    public void setParent(TreeNode parent) {
        originNode.setParent(parent);
    }

    @Override
    public List<TreeNode> getChildren() {
        return originNode.getChildren();
    }

    @Override
    public List<TreeNode> getAndInitChildren() {
        return originNode.getAndInitChildren();
    }

    @Override
    public void setChildren(List<TreeNode> children) {
        originNode.setChildren(children);
    }

    @Override
    public TreeItem<TreeNode> getTreeItem() {
        return originNode.getTreeItem();
    }

    @Override
    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        originNode.setTreeItem(treeItem);
    }

    @Override
    public String getPath() {
        return originNode.getPath();
    }

    @Override
    public void setPath(String path) {
        originNode.setPath(path);
    }

    @Override
    public byte[] getBytes() {
        return originNode.getBytes();
    }

    @Override
    public void setBytes(byte[] bytes) {
        originNode.setBytes(bytes);
    }

    @Override
    public String getMd5() {
        return originNode.getMd5();
    }

    @Override
    public void setMd5(String md5) {
        originNode.setMd5(md5);
    }

    @Override
    public long getSize() {
        return originNode.getSize();
    }

    @Override
    public void setSize(long size) {
        originNode.setSize(size);
    }

    @Override
    public boolean isExpanded() {
        return originNode.isExpanded();
    }

    @Override
    public void setExpanded(boolean expanded) {
        originNode.setExpanded(expanded);
    }

    @Override
    public TreeNodeStatus getStatus() {
        return originNode.getStatus();
    }

    @Override
    public void setStatus(TreeNodeStatus status) {
        originNode.setStatus(status);
    }
}
