package cn.cpoet.patch.assistant.view.content.parser;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;

import java.nio.charset.StandardCharsets;

/**
 * @author CPoet
 */
public class TextContentParser extends ContentParser {
    @Override
    public String parse(TreeNode node) {
        return new String(node.getBytes(), StandardCharsets.UTF_8);
    }
}
