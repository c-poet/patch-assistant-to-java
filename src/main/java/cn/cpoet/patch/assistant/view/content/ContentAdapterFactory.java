package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CPoet
 */
public class ContentAdapterFactory {


    private static ContentAdapterFactory defaultFactory;
    private final List<IContentAdapter> adapters = new ArrayList<>();

    public ContentAdapterFactory() {
        adapters.add(new ClassContentAdapter());
        adapters.add(new TextContentAdapter());
    }

    public IContentAdapter getAdapter(TreeKindNode node) {
        for (IContentAdapter adapter : adapters) {
            if (adapter.support(node)) {
                return adapter;
            }
        }
        return null;
    }

    public static ContentAdapterFactory defaultFactory() {
        if (defaultFactory == null) {
            defaultFactory = new ContentAdapterFactory();
        }
        return defaultFactory;
    }
}
