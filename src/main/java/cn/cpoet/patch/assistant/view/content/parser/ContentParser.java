package cn.cpoet.patch.assistant.view.content.parser;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author CPoet
 */
public abstract class ContentParser {
    /**
     * 解析内容
     *
     * @param node    需要解析的节点
     * @param charset 指定编码
     * @return 解析结果
     */
    public abstract String parse(TreeNode node, Charset charset);

    /**
     * 解析内容
     *
     * @param node 需要解析的节点
     * @return 解析结果
     */
    public String parse(TreeNode node) {
        return parse(node, StandardCharsets.UTF_8);
    }
}
