package cn.cpoet.patch.assistant.view.content.parser;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.util.CharsetUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;

import java.nio.charset.Charset;

/**
 * @author CPoet
 */
public class TextContentParser extends ContentParser {
    @Override
    public String parse(TreeNode node, Charset charset) {
        byte[] bytes = TreeNodeUtil.readNodeBytes(node);
        if (charset == null) {
            charset = CharsetUtil.getCharset(bytes);
        }
        return new String(bytes, charset);
    }
}
