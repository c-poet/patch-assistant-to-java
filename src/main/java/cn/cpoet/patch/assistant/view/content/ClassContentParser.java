package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.fernflower.SingleClassDecompiler;
import cn.cpoet.patch.assistant.view.tree.TreeNode;

import java.util.Collections;

/**
 * @author CPoet
 */
public class ClassContentParser extends ContentParser {

    @Override
    public String parse(TreeNode node) {
        SingleClassDecompiler decompiler = new SingleClassDecompiler(Collections.emptyMap());
        return decompiler.decompile(node.getBytes());
    }
}
