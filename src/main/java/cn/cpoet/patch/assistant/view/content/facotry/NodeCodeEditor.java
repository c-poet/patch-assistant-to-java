package cn.cpoet.patch.assistant.view.content.facotry;

import cn.cpoet.patch.assistant.control.code.CodeEditor;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;

/**
 * @author CPoet
 */
public class NodeCodeEditor extends CodeEditor {

    private TreeNode node;

    public TreeNode getNode() {
        return node;
    }

    public void setNode(TreeNode node) {
        this.node = node;
    }
}
