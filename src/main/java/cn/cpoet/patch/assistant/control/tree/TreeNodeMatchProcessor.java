package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.MappedNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.control.tree.node.VirtualNode;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.util.UIUtil;

/**
 * @author CPoet
 */
public abstract class TreeNodeMatchProcessor {
    protected final TotalInfo totalInfo;
    protected final TreeNode appNode;

    public TreeNodeMatchProcessor(TotalInfo totalInfo, TreeNode appNode) {
        this.totalInfo = totalInfo;
        this.appNode = appNode;
    }

    public abstract void exec();

    protected TreeNode createAndGetAppNode(TreeNode patchNode, TreeNode appNode) {
        TreeNode childNode;
        if (patchNode.isDir()) {
            VirtualNode virtualNode = new VirtualNode();
            virtualNode.setName(patchNode.getName());
            virtualNode.setModifyTime(patchNode.getModifyTime());
            virtualNode.setDir(true);
            childNode = virtualNode;
        } else {
            childNode = new MappedNode(patchNode);
        }
        TreeNode dirNode = appNode;
        while (!dirNode.isDir()) {
            dirNode = dirNode.getParent();
            if (dirNode == null) {
                break;
            }
        }
        if (dirNode == null) {
            childNode.setPath(FileNameUtil.SEPARATOR + childNode.getName());
        } else {
            childNode.setPath(FileNameUtil.joinPath(dirNode.getPath(), childNode.getName()));
        }
        if (childNode.isDir()) {
            childNode.setPath(childNode.getPath() + FileNameUtil.SEPARATOR);
        }
        childNode.setParent(appNode);
        appNode.getAndInitChildren().add(childNode);
        FileTreeItem childItem = new FileTreeItem();
        childNode.setTreeItem(childItem);
        childItem.setValue(childNode);
        UIUtil.runUI(() -> appNode.getTreeItem().getChildren().add(childItem));
        if (!patchNode.isDir()) {
            TreeNodeUtil.mappedNode(totalInfo, childNode, patchNode, TreeNodeType.ADD);
        }
        return childNode;
    }
}
