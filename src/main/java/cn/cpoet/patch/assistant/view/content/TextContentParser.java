package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

import java.nio.charset.StandardCharsets;

/**
 * @author CPoet
 */
public class TextContentParser extends ContentParser {
    @Override
    public String parse(TreeKindNode node) {
        return new String(node.getBytes(), StandardCharsets.UTF_8);
    }
}
