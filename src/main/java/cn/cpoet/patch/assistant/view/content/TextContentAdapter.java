package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

/**
 * @author CPoet
 */
public class TextContentAdapter implements IContentAdapter {

    @Override
    public boolean support(TreeKindNode node) {
        String name = node.getText();
        return name.endsWith(".txt")
                || name.endsWith(".yaml")
                || name.endsWith(".yml")
                || name.endsWith(".properties")
                || name.endsWith(".xml")
                || name.endsWith(".json")
                || name.endsWith(".html")
                || name.endsWith(".htm")
                || name.endsWith(".md");
    }

    @Override
    public String handle(TreeKindNode node) {
        return new String(node.getBytes());
    }
}
