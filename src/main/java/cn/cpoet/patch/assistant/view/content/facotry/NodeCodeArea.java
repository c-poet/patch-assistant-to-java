package cn.cpoet.patch.assistant.view.content.facotry;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import org.fxmisc.richtext.CodeArea;

/**
 * @author CPoet
 */
public class NodeCodeArea extends CodeArea {

    private TreeNode node;

    public TreeNode getNode() {
        return node;
    }

    public void setNode(TreeNode node) {
        this.node = node;
    }
}
