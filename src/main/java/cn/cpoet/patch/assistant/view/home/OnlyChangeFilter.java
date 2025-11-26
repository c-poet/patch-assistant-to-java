package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.control.tree.node.VirtualNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;

import java.util.function.Predicate;

/**
 * 仅显示变动节点
 *
 * @author CPoet
 */
public class OnlyChangeFilter implements Predicate<TreeNode> {

    public final static OnlyChangeFilter INSTANCE = new OnlyChangeFilter();

    @Override
    public boolean test(TreeNode node) {
        if (!Boolean.TRUE.equals(Configuration.getInstance().getIsOnlyChanges())) {
            return true;
        }
        return node.getMappedNode() != null || node instanceof VirtualNode;
    }
}
