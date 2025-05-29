package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.view.tree.FileTreeItem;
import cn.cpoet.patch.assistant.view.tree.TotalInfo;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

/**
 * 树形节点工具
 *
 * @author CPoet
 */
public abstract class TreeNodeUtil {

    private TreeNodeUtil() {
    }

    /**
     * 构建节点项
     *
     * @param rootItem 根节点项
     * @param treeNode 节点信息
     * @param <T>      节点类型
     */
    public static <T extends TreeNode> void buildNode(TreeItem<T> rootItem, T treeNode) {
        buildNode(rootItem, treeNode, node -> true);
    }

    /**
     * 构建节点项
     *
     * @param rootItem 根节点项
     * @param treeNode 节点信息
     * @param <T>      节点类型
     */
    @SuppressWarnings("unchecked")
    public static <T extends TreeNode> void buildNode(TreeItem<T> rootItem, T treeNode, Predicate<T> filter) {
        rootItem.setValue(treeNode);
        treeNode.setTreeItem((TreeItem<TreeNode>) rootItem);
        buildNodeChildren(rootItem, treeNode, filter);
    }

    /**
     * 构建树形子节点
     *
     * @param rootItem 根节点项
     * @param treeNode 节点信息
     * @param <T>      节点类型
     */
    public static <T extends TreeNode> void buildNodeChildren(TreeItem<T> rootItem, T treeNode) {
        buildNodeChildren(rootItem, treeNode, node -> true);
    }

    /**
     * 构建树形子节点
     *
     * @param rootItem 根节点项
     * @param treeNode 节点信息
     * @param filter   过滤器
     * @param <T>      节点类型
     */
    @SuppressWarnings("unchecked")
    public static <T extends TreeNode> void buildNodeChildren(TreeItem<T> rootItem, T treeNode, Predicate<T> filter) {
        if (treeNode.getChildren() != null && !treeNode.getChildren().isEmpty()) {
            treeNode.getChildren().forEach(node -> buildChildNode((TreeItem<TreeNode>) rootItem, node, (Predicate<TreeNode>) filter));
        }
    }

    /**
     * 构建树形子节点
     *
     * @param parent 父级节点项
     * @param node   节点信息
     * @param filter 过滤器
     */
    private static void buildChildNode(TreeItem<TreeNode> parent, TreeNode node, Predicate<TreeNode> filter) {
        if (node.getChildren() != null && node.getChildren().size() == 1) {
            StringBuilder sb = new StringBuilder();
            do {
                if (sb.length() > 0) {
                    sb.append(FileNameUtil.SEPARATOR);
                }
                sb.append(node.getText());
                node = node.getChildren().get(0);
            } while (node.getChildren() != null && node.getChildren().size() == 1);
            node.setText(sb.append(FileNameUtil.SEPARATOR).append(node.getText()).toString());
            buildChildNode(parent, node, filter);
            return;
        }
        TreeItem<TreeNode> childItem = new FileTreeItem();
        childItem.setValue(node);
        node.setTreeItem(childItem);
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            node.getChildren().forEach(child -> buildChildNode(childItem, child, filter));
        }
        if (!childItem.getChildren().isEmpty() || filter.test(node)) {
            parent.getChildren().add(childItem);
        } else {
            node.setTreeItem(null);
        }
    }

    /**
     * 根据路径查询节点
     *
     * @param node 节点
     * @param path 路径
     * @return 查询到的节点信息
     */
    public static TreeNode findNodeByPath(TreeNode node, String path) {
        if (!(node instanceof TreeKindNode)) {
            return null;
        }
        if (path.replaceAll("\\\\", FileNameUtil.SEPARATOR).equals(((TreeKindNode) node)
                .getPath().replaceAll("\\\\", FileNameUtil.SEPARATOR))) {
            return node;
        }
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (TreeNode child : node.getChildren()) {
                TreeNode target = findNodeByPath(child, path);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    /**
     * 展开所有节点
     *
     * @param rootItem 根节点
     */
    public static void expandedAllNode(TreeItem<TreeNode> rootItem) {
        rootItem.setExpanded(true);
        if (rootItem.getChildren() != null && !rootItem.getChildren().isEmpty()) {
            rootItem.getChildren().forEach(TreeNodeUtil::expandedAllNode);
        }
    }

    /**
     * 展开绑定的节点
     *
     * @param rootItem 根节点
     */
    public static boolean expandedMappedNode(TreeItem<TreeNode> rootItem) {
        boolean childExpanded = false;
        if (rootItem.getChildren() != null) {
            for (TreeItem<TreeNode> child : rootItem.getChildren()) {
                if (expandedMappedNode(child)) {
                    childExpanded = true;
                }
            }
        }
        if (childExpanded || rootItem.getValue().getMappedNode() != null) {
            rootItem.setExpanded(true);
            return true;
        }
        rootItem.setExpanded(false);
        return false;
    }

    /**
     * 存在绑定的节点时仅展开绑定的节点否则那个全部
     *
     * @param rootItem 根节点
     */
    public static void expendedMappedOrAllNode(TotalInfo totalInfo, TreeItem<TreeNode> rootItem) {
        if (totalInfo.isMappedAddOrModNode()) {
            expandedMappedNode(rootItem);
        } else {
            expandedAllNode(rootItem);
        }
    }
}
