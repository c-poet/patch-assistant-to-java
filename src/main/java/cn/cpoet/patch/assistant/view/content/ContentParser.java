package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

/**
 * @author CPoet
 */
public abstract class ContentParser {
    public String parse(TreeKindNode node) {
        return new String(node.getBytes());
    }
}
