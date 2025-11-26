package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.model.PatchSign;

/**
 * 补丁根节点信息
 *
 * @author CPoet
 */
public class PatchRootInfo {

    /**
     * readme节点
     */
    private TreeNode readmeNode;

    /**
     * 补丁签名信息
     */
    private PatchSign patchSign;

    public TreeNode getReadmeNode() {
        return readmeNode;
    }

    public void setReadmeNode(TreeNode readmeNode) {
        this.readmeNode = readmeNode;
    }

    public PatchSign getPatchSign() {
        return patchSign;
    }

    public void setPatchSign(PatchSign patchSign) {
        this.patchSign = patchSign;
    }
}
