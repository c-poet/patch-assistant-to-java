package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.fernflower.SingleClassDecompiler;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CPoet
 */
public class ClassContentAdapter implements IContentAdapter {

    private final static int SOURCE_CACHE_LIMIT = 1 << 4;

    /** Single threaded operation does not consider security issues */
    private final Map<String, String> sourceCache = new HashMap<>();

    @Override
    public boolean support(TreeKindNode node) {
        return node.getName().endsWith(FileExtConst.DOT_CLASS);
    }

    @Override
    public String handle(TreeKindNode node) {
        String md5 = node.getMd5();
        String s = sourceCache.get(md5);
        if (s != null) {
            return s;
        }
        SingleClassDecompiler decompiler = new SingleClassDecompiler(Collections.emptyMap());
        String decompile = decompiler.decompile(node.getBytes());
        if (sourceCache.size() >= SOURCE_CACHE_LIMIT) {
            sourceCache.clear();
        }
        sourceCache.put(md5, decompile);
        return decompile;
    }
}
