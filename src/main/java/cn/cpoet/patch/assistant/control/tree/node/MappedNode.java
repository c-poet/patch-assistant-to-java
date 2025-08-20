package cn.cpoet.patch.assistant.control.tree.node;

import java.time.LocalDateTime;

/**
 * 虚拟绑定节点
 *
 * @author CPoet
 */
public class MappedNode extends TreeNode {

    public MappedNode(TreeNode mappedNode) {
        setMappedNode(mappedNode);
    }

    @Override
    public boolean isDir() {
        return mappedNode.isDir();
    }

    @Override
    public LocalDateTime getModifyTime() {
        return mappedNode.getModifyTime();
    }

    @Override
    public void setMappedNode(TreeNode mappedNode) {
        super.setMappedNode(mappedNode);
        if (mappedNode != null) {
            setName(mappedNode.getName());
            setMd5(mappedNode.getMd5());
            setSize(mappedNode.getSize());
        }
    }
}
