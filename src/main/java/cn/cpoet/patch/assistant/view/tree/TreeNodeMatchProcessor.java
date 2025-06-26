package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * 树形节点匹配
 *
 * @author CPoet
 */
public class TreeNodeMatchProcessor {

    private final TotalInfo totalInfo;
    private final PatchPackService patchPackService;
    private final List<TreeItem<TreeNode>> appTreeItems;
    private final List<TreeItem<TreeNode>> patchTreeItems;

    public TreeNodeMatchProcessor(TotalInfo totalInfo, List<TreeItem<TreeNode>> appTreeItems, List<TreeItem<TreeNode>> patchTreeItems) {
        this.totalInfo = totalInfo;
        this.appTreeItems = appTreeItems;
        this.patchTreeItems = patchTreeItems;
        this.patchPackService = PatchPackService.getInstance();
    }

    public void exec() {
        if (CollectionUtil.isEmpty(patchTreeItems)) {
            return;
        }
        patchTreeItems.forEach(patchItem -> match(patchItem, appTreeItems));
    }

    private void match(TreeItem<TreeNode> patchItem, List<TreeItem<TreeNode>> appTreeItems) {
        TreeNode patchNode = patchItem.getValue();
        for (TreeItem<TreeNode> appItem : appTreeItems) {
            TreeNode appNode = appItem.getValue();
            if (patchPackService.matchPatchName(appNode, patchNode)) {

                return;
            }
        }
        createAppItem(patchItem, appTreeItems);
    }

    private void createAppItem(TreeItem<TreeNode> patchItem, List<TreeItem<TreeNode>> appTreeItems) {
        System.out.println("aaa");
        TreeNode patchNode = patchItem.getValue();
        System.out.println(patchNode.getName());
        TreeNode appNode = new VirtualMappedNode(patchNode);
        System.out.println("99999");
        FileTreeItem appItem = new FileTreeItem();
        System.out.println("1111111");
        appNode.setTreeItem(appItem);
        appItem.setValue(appNode);
        System.out.println("bbbbbbbbb");
        appTreeItems.add(appItem);
        if (patchNode.isDir()) {
            TreeNodeUtil.mappedNode(appNode, patchNode, TreeNodeStatus.ADD);
        } else {
            TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeStatus.ADD);
        }
        System.out.println(appNode.getName());
        patchItem.getChildren().forEach(childItem -> match(childItem, appItem.getChildren()));
    }
}
