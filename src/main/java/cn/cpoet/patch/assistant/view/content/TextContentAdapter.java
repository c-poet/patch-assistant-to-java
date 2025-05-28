package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

import java.util.Arrays;

/**
 * @author CPoet
 */
public class TextContentAdapter implements IContentAdapter {

    @Override
    public boolean support(TreeKindNode node) {
        String name = node.getName();
        return name.endsWith(".txt")
                || name.endsWith(".yaml")
                || name.endsWith(".yml")
                || name.endsWith(".properties")
                || name.endsWith(".xml")
                || name.endsWith(".json")
                || name.endsWith(".md");
    }

    @Override
    public String handle(TreeKindNode node) {
        return new String(node.getBytes());
    }
}
