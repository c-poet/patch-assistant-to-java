package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.JarInfoConst;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.ZipEntryNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 压缩包处理基类
 *
 * @author CPoet
 */
public abstract class BasePackService {

    public boolean buildNodeChildrenWithZip(TreeNode rootNode, boolean isPatch) {
        if (rootNode.getChildren() != null) {
            return false;
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(rootNode.getBytes());
             ZipInputStream zin = new ZipInputStream(in, Charset.forName("GBK"))) {
            doReadZipEntry(rootNode, zin, isPatch);
            return true;
        } catch (IOException ex) {
            throw new AppException("读取压缩文件失败", ex);
        }
    }

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
                .collect(Collectors.toMap(node -> FileNameUtil.getFileName(node.getPath()), Function.identity()));
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

    public void doReadZipEntry(TreeNode rootNode, ZipInputStream zin, boolean isPatch) throws IOException {
        ZipEntry zipEntry;
        TreeNode manifestNode = null;
        List<TreeNode> classes = new ArrayList<>();
        List<TreeNode> innerClasses = new ArrayList<>();
        Map<String, TreeNode> treeNodeMap = new HashMap<>();
        while ((zipEntry = zin.getNextEntry()) != null) {
            ZipEntryNode zipEntryNode = new ZipEntryNode();
            zipEntryNode.setName(FileNameUtil.getFileName(zipEntry.getName()));
            zipEntryNode.setText(zipEntryNode.getName());
            zipEntryNode.setPath(zipEntry.getName());
            zipEntryNode.setEntry(zipEntry);
            zipEntryNode.setPatch(isPatch);
            if (!zipEntry.isDirectory()) {
                zipEntryNode.setSize(zipEntry.getSize());
                zipEntryNode.setBytes(zin.readAllBytes());
                if (zipEntryNode.getName().endsWith(FileExtConst.DOT_CLASS)) {
                    if (zipEntryNode.getName().indexOf('$') != -1) {
                        innerClasses.add(zipEntryNode);
                        continue;
                    } else {
                        classes.add(zipEntryNode);
                    }
                }
            }
            TreeNode parentNode = treeNodeMap.getOrDefault(FileNameUtil.getDirPath(zipEntry.getName()), rootNode);
            if (JarInfoConst.MANIFEST_PATH.equals(zipEntry.getName())) {
                if (parentNode != rootNode) {
                    zipEntryNode.setParent(parentNode);
                    parentNode.getAndInitChildren().add(zipEntryNode);
                } else {
                    manifestNode = zipEntryNode;
                }
                continue;
            }
            if (JarInfoConst.META_INFO_DIR.equals(zipEntry.getName())) {
                if (manifestNode != null) {
                    manifestNode.setParent(zipEntryNode);
                    zipEntryNode.getAndInitChildren().add(manifestNode);
                }
            }
            zipEntryNode.setParent(parentNode);
            parentNode.getAndInitChildren().add(zipEntryNode);
            if (zipEntry.isDirectory()) {
                treeNodeMap.put(zipEntry.getName().substring(0, zipEntry.getName().length() - 1), zipEntryNode);
            }
        }
        if (CollectionUtil.isNotEmpty(classes) && CollectionUtil.isNotEmpty(innerClasses)) {
            handleInnerClass(classes, innerClasses);
        }
    }
}
