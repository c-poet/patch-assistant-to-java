package cn.cpoet.patch.assistant.view.tree;

import java.nio.charset.StandardCharsets;

/**
 * 补丁包信息
 *
 * @author CPoet
 */
public class PatchTreeInfo extends TreeInfo {

    /**
     * 自定义根节点
     */
    private TreeNode customRootNode;

    /**
     * Readme节点信息
     */
    private TreeNode readMeNode;

    public TreeNode getCustomRootNode() {
        return customRootNode;
    }

    public void setCustomRootNode(TreeNode customRootNode) {
        this.customRootNode = customRootNode;
    }

    public TreeNode getReadMeNode() {
        return readMeNode;
    }

    public void setReadMeNode(TreeNode readMeNode) {
        this.readMeNode = readMeNode;
    }

    /**
     * 获取当前的根节点
     *
     * @return 根节点
     */
    public TreeNode getCurRootNode() {
        return customRootNode != null ? customRootNode : getRootNode();
    }

    /**
     * 读取ReadMe文本内容
     *
     * @return Read文本内容
     */
    public String getReadMeText() {
        return readMeNode == null ? null : new String(readMeNode.getBytes(), StandardCharsets.UTF_8);
    }
}
