package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.JarInfoConst;
import cn.cpoet.patch.assistant.constant.SpringConst;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.fernflower.FileClassDecompiler;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.progress.ProgressContext;

import java.io.File;
import java.util.*;

/**
 * Jar包反编译
 *
 * @author CPoet
 */
public class JarDecompilerService {

    public static final JarDecompilerService INSTANCE = new JarDecompilerService();

    /**
     * 反编辑
     *
     * @param context 进度上下文
     * @param jarNode Jar包节点
     * @param file    保存到的目录
     */
    public void decompile(ProgressContext context, TreeNode jarNode, File file) {
        UIUtil.runNotUI(() -> ExceptionUtil.runAsTryCatch(() -> {
            try {
                TreeNode metaInfNode = null;
                TreeNode bootInfNode = null;
                context.step("Find BOO-INF and MATA-INFO");
                for (TreeNode child : jarNode.getChildren()) {
                    if (JarInfoConst.META_INFO_DIR.equals(child.getPath())) {
                        metaInfNode = child;
                    } else if (SpringConst.BOOT_INF_PATH.equals(child.getPath())) {
                        bootInfNode = child;
                    }
                    if (metaInfNode != null && bootInfNode != null) {
                        break;
                    }
                }
                if (bootInfNode != null) {
                    context.step("Decompile according to the Spring project format");
                    decompileSpring(context, metaInfNode, bootInfNode, file);
                } else {
                    context.step("Decompile according to normal projects");
                    decompileRoutine(context, metaInfNode, jarNode, file);
                }
                context.end(true);
            } catch (Exception e) {
                context.step(e.getMessage());
                context.end(false);
                throw e;
            }
        }));
    }

    public void decompileSpring(ProgressContext context, TreeNode metaInfNode, TreeNode bootInfNode, File file) {
        TreeNode classesNode = null;
        TreeNode libNode = null;
        if (CollectionUtil.isNotEmpty(bootInfNode.getChildren())) {
            for (TreeNode child : bootInfNode.getChildren()) {
                if (SpringConst.CLASSES_PATH.equals(child.getPath())) {
                    classesNode = child;
                } else if (SpringConst.LIB_PATH.equals(child.getPath())) {
                    libNode = child;
                }
                if (classesNode != null && libNode != null) {
                    break;
                }
            }
        }
        if (classesNode != null) {
            context.step("classes node: " + classesNode.getPath());
            doDecompile(context, classesNode, file);
        }
        if (libNode != null) {
            context.step("lib node: " + libNode.getPath());
            saveToLibPath(context, libNode, file);
        }
        saveToMetaInfo(context, metaInfNode, file);
    }

    public void saveToLibPath(ProgressContext context, TreeNode node, File file) {
        if (CollectionUtil.isEmpty(node.getChildren())) {
            return;
        }
        saveToPath(context, node, new File(file, JarInfoConst.SOURCE_LIB));
    }

    public void saveToMetaInfo(ProgressContext context, TreeNode node, File parent) {
        if (node == null || CollectionUtil.isEmpty(node.getChildren())) {
            return;
        }
        File file = new File(parent, FileNameUtil.joinPath(JarInfoConst.SOURCE_SRC, JarInfoConst.META_INFO));
        node.getChildren().forEach(child -> saveToPath(context, child, file));
    }

    public void saveToPath(ProgressContext context, TreeNode node, File parent) {
        if (CollectionUtil.isEmpty(node.getChildren())) {
            return;
        }
        for (TreeNode child : node.getChildren()) {
            if (child.isDir() && CollectionUtil.isNotEmpty(child.getChildren())) {
                saveToPath(context, child, new File(parent, child.getName()));
            } else {
                if (isPomFileNode(child)) {
                    context.step("Skip pom file: " + child.getName());
                    continue;
                }
                context.step("Save file: " + child.getName());
                FileUtil.writeFile(new File(parent, child.getName()), child::consumeBytes);
            }
        }
    }

    public boolean isPomFileNode(TreeNode node) {
        return JarInfoConst.MVN_POM_XML.equals(node.getName()) || JarInfoConst.MVN_POM_PROPERTIES.equals(node.getName());
    }

    public void decompileRoutine(ProgressContext context, TreeNode metaInfoNode, TreeNode jarNode, File file) {
        // 普通jar包除META-INF目录外，其余目录全部反编译
        for (TreeNode child : jarNode.getChildren()) {
            if (child != metaInfoNode) {
                doDecompile(context, child, file);
            }
        }
        saveToMetaInfo(context, metaInfoNode, file);
    }

    public void doDecompile(ProgressContext context, TreeNode node, File file) {
        File srcFile = new File(file, JarInfoConst.SOURCE_SRC);
        doDecompile(context, srcFile, srcFile, node);
    }

    public void doDecompile(ProgressContext context, File srcFile, File parent, TreeNode node) {
        if (node.isDir()) {
            if (CollectionUtil.isNotEmpty(node.getChildren())) {
                for (TreeNode child : node.getChildren()) {
                    doDecompile(context, srcFile, new File(parent, child.getName()), child);
                }
            }
            return;
        }
        if (!node.getName().endsWith(FileExtConst.DOT_CLASS)) {
            saveToPath(context, node, parent);
            return;
        }
        List<byte[]> innerBytes = Collections.emptyList();
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            innerBytes = new ArrayList<>();
            Queue<TreeNode> nodeQueue = new LinkedList<>(node.getChildren());
            while (!nodeQueue.isEmpty()) {
                TreeNode poll = nodeQueue.poll();
                innerBytes.add(TreeNodeUtil.readNodeBytes(poll));
            }
        }
        context.step("Decompile: " + node.getName());
        new FileClassDecompiler(srcFile, Collections.emptyMap()).decompile(TreeNodeUtil.readNodeBytes(node), innerBytes);
    }
}
