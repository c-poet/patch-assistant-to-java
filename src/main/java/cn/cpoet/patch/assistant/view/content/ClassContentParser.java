package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.fernflower.SingleClassDecompiler;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

import java.util.Collections;

/**
 * @author CPoet
 */
public class ClassContentParser extends ContentParser {

    @Override
    public String parse(TreeKindNode node) {
        SingleClassDecompiler decompiler = new SingleClassDecompiler(Collections.emptyMap());
        return decompiler.decompile(node.getBytes());
    }
}
