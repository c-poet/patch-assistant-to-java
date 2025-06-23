package cn.cpoet.patch.assistant.view.tree;

/**
 * 应用信息
 *
 * @author CPoet
 */
public class AppTreeInfo extends TreeInfo {
    /**
     * 应用补丁签名节点
     */
    private TreeNode patchUpSignNode;

    public TreeNode getPatchUpSignNode() {
        return patchUpSignNode;
    }

    public void setPatchUpSignNode(TreeNode patchUpSignNode) {
        this.patchUpSignNode = patchUpSignNode;
    }
}
