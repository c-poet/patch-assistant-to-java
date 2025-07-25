package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import javafx.application.Platform;

import java.util.List;

/**
 * 树形节点匹配
 *
 * @author CPoet
 */
public class TreeNodeMatchProcessor {

    private final TotalInfo totalInfo;
    private final TreeNode appNode;
    private final List<TreeNode> patchNodes;
    private final PatchPackService patchPackService;

    public TreeNodeMatchProcessor(TotalInfo totalInfo, TreeNode appNode, List<TreeNode> patchNodes) {
        this.totalInfo = totalInfo;
        this.appNode = appNode;
        this.patchNodes = patchNodes;
        this.patchPackService = PatchPackService.getInstance();
    }

    public void exec() {
        if (CollectionUtil.isEmpty(patchNodes)) {
            return;
        }
        Platform.runLater(() -> {
            if (patchNodes.size() == 1 && patchNodes.get(0).isDir() && appNode.isDir()) {
                matchWithDir(patchNodes.get(0), appNode);
                return;
            }
            TreeNode tarAppNode = appNode.isDir() ? appNode : appNode.getParent();
            patchNodes.forEach(patchNode -> match(patchNode, tarAppNode));
        });
    }

    private void matchWithDir(TreeNode patchNode, TreeNode appNode) {
        if (CollectionUtil.isEmpty(patchNode.getChildren())) {
            return;
        }
        patchNode.getChildren().forEach(childNode -> match(childNode, appNode));
    }

    private void match(TreeNode patchNode, TreeNode appNode) {
        if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
            for (TreeNode childNode : appNode.getChildren()) {
                if (patchPackService.matchPatchName(childNode, patchNode)) {
                    matchMapping(childNode, patchNode);
                    return;
                }
            }
        }
        createAppItem(patchNode, appNode);
    }

    private void matchMapping(TreeNode appNode, TreeNode patchNode) {
        if (appNode instanceof VirtualTreeNode || appNode instanceof VirtualMappedNode) {
            TreeNodeUtil.removeNodeChild(appNode);
            createAppItem(patchNode, appNode);
            return;
        }
        TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.MOD);
        matchWithDir(patchNode, appNode);
    }

    private void createAppItem(TreeNode patchNode, TreeNode appNode) {
        TreeNode childNode;
        if (patchNode.isDir()) {
            VirtualTreeNode virtualTreeNode = new VirtualTreeNode();
            virtualTreeNode.setName(patchNode.getName());
            virtualTreeNode.setText(patchNode.getText());
            virtualTreeNode.setModifyTime(patchNode.getModifyTime());
            virtualTreeNode.setDir(true);
            childNode = virtualTreeNode;
        } else {
            childNode = new VirtualMappedNode(patchNode);
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
        appNode.getTreeItem().getChildren().add(childItem);
        if (!patchNode.isDir()) {
            TreeNodeUtil.mappedNode(totalInfo, childNode, patchNode, TreeNodeType.ADD);
        }
        if (CollectionUtil.isNotEmpty(patchNode.getChildren())) {
            patchNode.getChildren().forEach(node -> match(node, childNode));
        }
    }
}
