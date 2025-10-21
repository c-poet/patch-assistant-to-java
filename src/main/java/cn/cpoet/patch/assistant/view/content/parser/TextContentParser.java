package cn.cpoet.patch.assistant.view.content.parser;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;

import java.nio.charset.Charset;

/**
 * @author CPoet
 */
public class TextContentParser extends ContentParser {
    @Override
    public String parse(TreeNode node, Charset charset) {
        return new String(TreeNodeUtil.readNodeBytes(node), charset);
    }
}
