package cn.cpoet.patch.assistant.view.content.parser;

import cn.cpoet.patch.assistant.fernflower.SingleClassDecompiler;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * @author CPoet
 */
public class ClassContentParser extends ContentParser {

    @Override
    public String parse(TreeNode node, Charset charset) {
        SingleClassDecompiler decompiler = new SingleClassDecompiler(Collections.emptyMap());
        if (CollectionUtil.isEmpty(node.getChildren())) {
            return decompiler.decompile(node.getBytes());
        }
        Stack<TreeNode> childStack = new Stack<>();
        List<byte[]> innerBytes = new ArrayList<>();
        childStack.addAll(node.getChildren());
        while (!childStack.empty()) {
            TreeNode innerClassNode = childStack.pop();
            innerBytes.add(innerClassNode.getBytes());
            if (CollectionUtil.isNotEmpty(innerClassNode.getChildren())) {
                childStack.addAll(innerClassNode.getChildren());
            }
        }
        return decompiler.decompile(node.getBytes(), innerBytes);
    }
}
