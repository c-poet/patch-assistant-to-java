package cn.cpoet.patch.assistant.view.tree;

import java.time.LocalDateTime;

/**
 * 虚拟绑定节点
 *
 * @author CPoet
 */
public class VirtualMappedNode extends TreeKindNode {

    public VirtualMappedNode(TreeKindNode mappedNode) {
        setMappedNode(mappedNode);
    }

    @Override
    public boolean isDir() {
        return ((TreeKindNode) mappedNode).isDir();
    }

    @Override
    public LocalDateTime getModifyTime() {
        return ((TreeKindNode) mappedNode).getModifyTime();
    }

    @Override
    public void setMappedNode(TreeNode mappedNode) {
        super.setMappedNode(mappedNode);
        setName(mappedNode.getName());
        setText(mappedNode.getText());
        setMd5(mappedNode.getText());
        setBytes(((TreeKindNode) mappedNode).getBytes());
    }
}
