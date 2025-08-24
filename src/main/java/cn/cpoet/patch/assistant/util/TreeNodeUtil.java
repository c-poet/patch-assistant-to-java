package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.control.tree.FileTreeItem;
import cn.cpoet.patch.assistant.control.tree.TotalInfo;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.MappedNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.control.tree.node.VirtualNode;
import cn.cpoet.patch.assistant.service.compress.FileDecompressor;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.List;
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
     * 从父级节点中移出指定节点
     *
     * @param node 节点
     */
    public static void removeNodeChild(TreeNode node) {
        TreeNode parent = node.getParent();
        if (parent != null) {
            parent.getChildren().remove(node);
            TreeItem<TreeNode> treeItem = node.getTreeItem();
            if (treeItem != null) {
                UIUtil.runUI(() -> parent.getTreeItem().getChildren().remove(treeItem));
            }
        }
    }

    /**
     * 清理绑定的节点
     *
     * @param totalInfo 统计信息
     * @param node      节点
     */
    public static void deepCleanMappedNode(TotalInfo totalInfo, TreeNode node) {
        deepCleanMappedNode(totalInfo, node, null);
    }

    /**
     * 清理绑定的节点
     *
     * @param totalInfo 统计信息
     * @param node      节点
     * @param filter    自定义过滤器
     */
    public static void deepCleanMappedNode(TotalInfo totalInfo, TreeNode node, Predicate<TreeNode> filter) {
        if (node == null) {
            return;
        }
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            node.getChildren().forEach(child -> deepCleanMappedNode(totalInfo, child, filter));
        }
        if (!node.isDir()) {
            TreeNodeType nodeType = node.getType();
            UIUtil.runUI(() -> totalInfo.decrTotal(nodeType));
        }
        cleanMappedNode(node.getMappedNode(), filter);
        cleanMappedNode(node, filter);
    }

    /**
     * 清理绑定的节点
     *
     * @param node 节点
     */
    public static void cleanMappedNode(TreeNode node, Predicate<TreeNode> filter) {
        if (node == null || filter != null && filter.test(node)) {
            return;
        }
        node.setType(TreeNodeType.NONE);
        node.setMappedNode(null);
        if (!node.isPatch() && (node instanceof VirtualNode || node instanceof MappedNode)) {
            removeNodeChild(node);
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
    public static void mappedNode(TotalInfo totalInfo, TreeNode node1, TreeNode node2, TreeNodeType status) {
        mappedNode(node1, node2, status);
        if (!node1.isDir()) {
            UIUtil.runUI(() -> totalInfo.incrTotal(status));
        }
    }

    /**
     * 更新绑定的节点和状态
     *
     * @param node1  绑定的节点1
     * @param node2  绑定的节点2
     * @param status 状态
     */
    public static void mappedNode(TreeNode node1, TreeNode node2, TreeNodeType status) {
        node1.setMappedNode(node2);
        node2.setMappedNode(node1);
        node1.setType(status);
        node2.setType(status);
    }

    /**
     * 统计文件信息
     *
     * @param totalInfo 统计信息
     * @param node      节点
     * @param type      状态
     */
    public static void countNodeType(TotalInfo totalInfo, TreeNode node, TreeNodeType type) {
        if (!node.isDir()) {
            totalInfo.incrTotal(type);
        }
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            node.getChildren().forEach(child -> countNodeType(totalInfo, child, type));
        }
    }

    /**
     * 统计并设置文件信息
     *
     * @param totalInfo 统计信息
     * @param node      节点
     * @param type      状态
     */
    public static void countAndSetNodeType(TotalInfo totalInfo, TreeNode node, TreeNodeType type) {
        node.setType(type);
        if (!node.isDir()) {
            UIUtil.runUI(() -> totalInfo.incrTotal(type));
        }
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            node.getChildren().forEach(child -> countAndSetNodeType(totalInfo, child, type));
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
        rootItem.setExpanded(treeNode.isExpanded());
        treeNode.setTreeItem((TreeItem<TreeNode>) rootItem);
        bindTreeNodeAndItem(treeNode, (TreeItem<TreeNode>) rootItem);
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
        TreeItem<TreeNode> childItem = new FileTreeItem();
        bindTreeNodeAndItem(node, childItem);
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

    public static void bindTreeNodeAndItem(TreeNode node, TreeItem<TreeNode> item) {
        item.setValue(node);
        item.setExpanded(node.isExpanded());
        node.setTreeItem(item);
        item.expandedProperty().addListener((observableValue, oldVal, newVal) -> node.setExpanded(newVal));
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
     * 存在绑定的节点时仅展开绑定的节点否则展开根节点
     *
     * @param totalInfo 统计信息
     * @param rootItem  根节点
     * @param treeItems 所有自定义根列表
     */
    public static void expendedMappedOrCurRoot(TotalInfo totalInfo, TreeItem<TreeNode> rootItem, List<TreeItem<TreeNode>> treeItems) {
        if (totalInfo.isMappedAddOrModNode()) {
            expandedMappedNode(rootItem);
            return;
        }
        for (TreeItem<TreeNode> treeItem : treeItems) {
            treeItem.setExpanded(true);
        }
    }

    /**
     * 判断指定节点是否处于自定义根节点下面
     *
     * @param treeNode 指定
     * @return 是否处理自定义根节点下面
     */
    public static boolean isNotUnderCustomRoot(TreeNode treeNode) {
        TreeNode parent = treeNode.getParent();
        while (parent != null) {
            if (TreeNodeType.CUSTOM_ROOT.equals(parent.getType())) {
                return false;
            }
            parent = parent.getParent();
        }
        return true;
    }

    /**
     * 判断是否是压缩文件节点
     *
     * @param treeNode 节点
     * @return 是否是压缩文件节点
     */
    public static boolean isCompressNode(TreeNode treeNode) {
        return FileDecompressor.isCompressFile(treeNode.getName());
    }

    /**
     * 读取节点相关的文件内容并设置hash值
     *
     * @param node 节点
     * @param file 文件
     * @return 节点内容
     */
    public static byte[] readNodeFile(TreeNode node, File file) {
        byte[] data = FileUtil.readFile(file);
        node.setSize(data.length);
        if (node.getMd5() == null) {
            node.setMd5(HashUtil.md5(data));
        }
        return data;
    }
}
