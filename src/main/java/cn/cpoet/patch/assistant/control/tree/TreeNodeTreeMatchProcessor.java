package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.service.PatchPackService;

import java.util.List;

/**
 * 树形节点匹配
 *
 * @author CPoet
 */
public class TreeNodeTreeMatchProcessor extends TreeNodeMatchProcessor<TreeNode> {

    public TreeNodeTreeMatchProcessor(TotalInfo totalInfo, AppTreeInfo appTreeInfo, TreeNode appNode, List<TreeNode> patchNodes) {
        super(totalInfo, appTreeInfo, appNode, patchNodes);
    }

    @Override
    protected boolean isDirectory(TreeNode patchElement) {
        return patchElement.isDir();
    }

    @Override
    protected List<TreeNode> listChildren(TreeNode patchElement) {
        return patchElement.getChildren();
    }

    @Override
    protected boolean isMatch(TreeNode appNode, TreeNode patchElement) {
        return PatchPackService.INSTANCE.matchPatchName(appNode, patchElement);
    }

    @Override
    protected TreeNode getElementNode(TreeNode patchElement) {
        return patchElement;
    }
}
