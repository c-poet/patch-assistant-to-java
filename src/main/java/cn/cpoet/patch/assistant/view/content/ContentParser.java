package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.view.tree.TreeKindNode;

/**
 * @author CPoet
 */
public abstract class ContentParser {
    /**
     * 解析内容
     *
     * @param node 需要解析的节点
     * @return 解析结果
     */
    public abstract String parse(TreeKindNode node);
}
