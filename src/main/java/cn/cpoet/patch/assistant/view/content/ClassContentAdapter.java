package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.fernflower.SingleClassDecompiler;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

import java.util.Collections;

/**
 * @author CPoet
 */
public class ClassContentAdapter implements IContentAdapter {

    private final SingleClassDecompiler decompiler = new SingleClassDecompiler(Collections.emptyMap());

    @Override
    public boolean support(TreeKindNode node) {
        return node.getName().endsWith(FileExtConst.DOT_CLASS);
    }

    @Override
    public String handle(TreeKindNode node) {
        return decompiler.decompile(node.getBytes());
    }
}
