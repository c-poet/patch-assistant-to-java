package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.view.tree.FileTreeItem;
import cn.cpoet.patch.assistant.view.tree.TotalInfo;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.TreeNodeStatus;
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
     * 清理绑定的节点
     *
     * @param node 节点
     */
    public static void cleanMappedNode(TreeNode node) {
        if (node == null) {
            return;
        }
        node.setMappedNode(null);
        node.setStatus(TreeNodeStatus.NONE);
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            node.getChildren().forEach(TreeNodeUtil::cleanMappedNode);
        }
    }

    /**
     * 更新绑定的节点和状态
     *
     * @param totalInfo 统计信息
     * @param node1     绑定的节点1
     * @param node2     绑定的节点2
     * @param status    状态
     */
    public static void mappedNode(TotalInfo totalInfo, TreeNode node1, TreeNode node2, TreeNodeStatus status) {
        mappedNode(node1, node2, status);
        totalInfo.incrTotal(status);
    }

    /**
     * 更新绑定的节点和状态
     *
     * @param node1  绑定的节点1
     * @param node2  绑定的节点2
     * @param status 状态
     */
    public static void mappedNode(TreeNode node1, TreeNode node2, TreeNodeStatus status) {
        node1.setMappedNode(node2);
        node2.setMappedNode(node1);
        node1.setStatus(status);
        node2.setStatus(status);
    }

    /**
     * 统计文件信息
     *
     * @param totalInfo 统计信息
     * @param node      节点
     * @param status    状态
     */
    public static void countNodeStatus(TotalInfo totalInfo, TreeNode node, TreeNodeStatus status) {
        if (!node.isDir()) {
            totalInfo.incrTotal(status);
            return;
        }
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            node.getChildren().forEach(child -> countNodeStatus(totalInfo, child, status));
        }
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
    public static void buildChildNode(TreeItem<TreeNode> parent, TreeNode node, Predicate<TreeNode> filter) {
        buildChildNode(parent, -1, node, filter);
    }

    /**
     * 构建树形子节点
     *
     * @param parent 父级节点项
     * @param node   节点信息
     * @param filter 过滤器
     */
    public static void buildChildNode(TreeItem<TreeNode> parent, int index, TreeNode node, Predicate<TreeNode> filter) {
        if (node.getChildren() != null && node.isDir() && node.getChildren().size() == 1) {
            StringBuilder sb = new StringBuilder();
            do {
                if (sb.length() > 0) {
                    sb.append(FileNameUtil.SEPARATOR);
                }
                sb.append(node.getName());
                node = node.getChildren().get(0);
            } while (node.getChildren() != null && node.isDir() && node.getChildren().size() == 1);
            node.setText(sb.append(FileNameUtil.SEPARATOR).append(node.getName()).toString());
        } else {
            node.setText(node.getName());
        }
        TreeItem<TreeNode> childItem = new FileTreeItem();
        childItem.setValue(node);
        node.setTreeItem(childItem);
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            node.getChildren().forEach(child -> buildChildNode(childItem, child, filter));
        }
        if (!childItem.getChildren().isEmpty() || filter.test(node)) {
            if (index != -1) {
                parent.getChildren().add(index, childItem);
            } else {
                parent.getChildren().add(childItem);
            }
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
        if (path.replaceAll("\\\\", FileNameUtil.SEPARATOR).equals(node.getPath().replaceAll("\\\\", FileNameUtil.SEPARATOR))) {
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
