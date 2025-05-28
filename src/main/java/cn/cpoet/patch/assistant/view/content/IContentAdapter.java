package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

/**
 * @author CPoet
 */
public interface IContentAdapter {
    boolean support(TreeKindNode node);

    String handle(TreeKindNode node);
}
