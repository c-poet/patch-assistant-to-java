package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.JarInfoConst;
import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.service.compress.CompressNodeFactory;
import cn.cpoet.patch.assistant.service.compress.FileDecompressor;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 压缩包处理基类
 *
 * @author CPoet
 */
public abstract class BasePackService {

    /**
     * 处理内部类
     *
     * @param parentNode   父级节点
     * @param innerClasses 内部类列表
     */
    public void handleInnerClass(TreeNode parentNode, List<TreeNode> innerClasses) {
        if (CollectionUtil.isNotEmpty(parentNode.getChildren())) {
            handleInnerClass(parentNode.getChildren(), innerClasses);
        }
    }

    /**
     * 处理内部类
     *
     * @param classes      普通类列表
     * @param innerClasses 内部类列表
     */
    public void handleInnerClass(List<TreeNode> classes, List<TreeNode> innerClasses) {
        Map<String, TreeNode> classMap = classes.stream()
                .filter(node -> node.getName().endsWith(FileExtConst.DOT_CLASS))
                .collect(Collectors.toMap(node -> FileNameUtil.getName(node.getPath()), Function.identity()));
        handleInnerClass(classMap, innerClasses, 1);
    }

    /**
     * 处理内部类
     *
     * @param classMap     普通类集合
     * @param innerClasses 内部类列表
     * @param level        级数
     */
    public void handleInnerClass(Map<String, TreeNode> classMap, List<TreeNode> innerClasses, int level) {
        Iterator<TreeNode> it = innerClasses.iterator();
        while (it.hasNext()) {
            TreeNode innerClassNode = it.next();
            int[] indexAndCount = StringUtil.lastIndexOfAndCount(innerClassNode.getPath(), '$');
            if (indexAndCount[1] > level) {
                continue;
            }
            if (indexAndCount[1] == level) {
                TreeNode classNode = classMap.get(innerClassNode.getPath().substring(0, indexAndCount[0]));
                if (classNode != null) {
                    classNode.getAndInitChildren().add(innerClassNode);
                    classMap.put(FileNameUtil.getName(innerClassNode.getPath()), innerClassNode);
                }
            }
            it.remove();
        }
        if (CollectionUtil.isNotEmpty(innerClasses)) {
            handleInnerClass(classMap, innerClasses, level + 1);
        }
    }

    public boolean buildChildrenWithCompress(TreeNode rootNode, boolean isPatch) {
        if (rootNode.getChildren() != null) {
            return false;
        }
        rootNode.consumeInputStream(in -> buildChildrenWithCompress(rootNode, in, isPatch));
        return true;
    }

    private TreeNode getOrCreateParentNode(TreeNode rootNode, Map<String, TreeNode> parentNodeMap, TreeNode node, boolean isPatch) {
        if (node.isDir()) {
            TreeNode treeNode = parentNodeMap.get(FileNameUtil.unPerfectDirPath(node.getPath()));
            if (treeNode != null) {
                treeNode.getParent().getChildren().remove(treeNode);
                if (CollectionUtil.isNotEmpty(treeNode.getChildren())) {
                    treeNode.getChildren().forEach(childNode -> {
                        childNode.setParent(node);
                        node.getAndInitChildren().add(childNode);
                    });
                }
                return treeNode.getParent();
            }
        }
        String parentPath = FileNameUtil.getDirPath(node.getPath());
        if (StringUtil.isBlank(parentPath)) {
            return rootNode;
        }
        TreeNode parentNode = parentNodeMap.get(parentPath);
        if (parentNode != null) {
            return parentNode;
        }
        CompressNode compressNode = new CompressNode();
        compressNode.setName(FileNameUtil.getFileName(parentPath));
        compressNode.setDir(true);
        compressNode.setPatch(isPatch);
        compressNode.setPath(parentPath);
        compressNode.setCreateTime(LocalDateTime.now());
        compressNode.setAccessTime(compressNode.getCreateTime());
        compressNode.setModifyTime(compressNode.getCreateTime());
        parentNode = getOrCreateParentNode(rootNode, parentNodeMap, compressNode, isPatch);
        compressNode.setParent(parentNode);
        parentNode.getAndInitChildren().add(compressNode);
        parentNodeMap.put(parentPath, compressNode);
        return compressNode;
    }

    public void buildChildrenWithCompress(TreeNode rootNode, InputStream in, boolean isPatch) throws IOException {
        List<TreeNode> classes = new ArrayList<>();
        List<TreeNode> innerClasses = new ArrayList<>();
        Map<String, TreeNode> treeNodeMap = new HashMap<>();
        AtomicReference<TreeNode> manifestNodeRef = new AtomicReference<>();
        FileDecompressor fileDecompressor = FileDecompressor.getInstance(rootNode.getName());
        fileDecompressor.decompress(in, (entry, zin) -> {
            CompressNode node = CompressNodeFactory.getInstance(entry).create(entry, zin);
            node.setPatch(isPatch);
            if (!node.isDir()) {
                if (node.getName().endsWith(FileExtConst.DOT_CLASS)) {
                    if (node.getName().indexOf('$') != -1) {
                        innerClasses.add(node);
                        return;
                    } else {
                        classes.add(node);
                    }
                }
            }
            TreeNode parentNode = getOrCreateParentNode(rootNode, treeNodeMap, node, isPatch);
            if (JarInfoConst.MANIFEST_PATH.equals(node.getName())) {
                if (parentNode != rootNode) {
                    node.setParent(parentNode);
                    parentNode.getAndInitChildren().add(node);
                } else {
                    manifestNodeRef.set(node);
                }
                return;
            }
            if (JarInfoConst.META_INFO_DIR.equals(node.getName())) {
                if (manifestNodeRef.get() != null) {
                    manifestNodeRef.get().setParent(node);
                    node.getAndInitChildren().add(manifestNodeRef.get());
                }
            }
            node.setParent(parentNode);
            parentNode.getAndInitChildren().add(node);
            if (node.isDir()) {
                treeNodeMap.put(FileNameUtil.unPerfectDirPath(node.getPath()), node);
            }
        });
        if (CollectionUtil.isNotEmpty(classes) && CollectionUtil.isNotEmpty(innerClasses)) {
            handleInnerClass(classes, innerClasses);
        }
    }
}
