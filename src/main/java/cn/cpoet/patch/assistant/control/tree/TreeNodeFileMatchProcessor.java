package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.FileDirectNode;
import cn.cpoet.patch.assistant.control.tree.node.MappedNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.control.tree.node.VirtualNode;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.*;

import java.io.File;
import java.util.List;

/**
 * 树形节点和文件匹配
 *
 * @author CPoet
 */
public class TreeNodeFileMatchProcessor extends TreeNodeMatchProcessor {

    private final List<File> files;

    public TreeNodeFileMatchProcessor(TotalInfo totalInfo, TreeNode appNode, List<File> files) {
        super(totalInfo, appNode);
        this.files = files;
    }

    @Override
    public void exec() {
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        if (files.size() == 1 && files.get(0).isDirectory() && appNode.isDir()) {
            matchWithDir(files.get(0), appNode);
            return;
        }
        TreeNode tarAppNode = appNode.isDir() ? appNode : appNode.getParent();
        files.forEach(file -> match(file, tarAppNode));
    }

    private void matchWithDir(File file, TreeNode appNode) {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File childFile : files) {
            match(childFile, appNode);
        }
    }

    private void match(File file, TreeNode appNode) {
        if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
            for (TreeNode childNode : appNode.getChildren()) {
                if (PatchPackService.INSTANCE.matchPatchName(childNode, file.getName())) {
                    matchMapping(childNode, file);
                    return;
                }
            }
        }
        createAppItem(file, appNode);
    }

    private void matchMapping(TreeNode appNode, File file) {
        if (appNode instanceof VirtualNode || appNode instanceof MappedNode) {
            TreeNodeUtil.removeNodeChild(appNode);
            createAppItem(file, appNode);
            return;
        }
        FileDirectNode patchNode = createNode(file);
        TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.MOD);
        matchWithDir(file, appNode);
    }

    private void createAppItem(File file, TreeNode appNode) {
        FileDirectNode patchNode = createNode(file);
        TreeNode childNode = createAndGetAppNode(patchNode, appNode);
        if (patchNode.isDir()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    match(childFile, childNode);
                }
            }
        }
    }

    private FileDirectNode createNode(File file) {
        FileDirectNode fileDirectNode = new FileDirectNode();
        fileDirectNode.setName(file.getName());
        fileDirectNode.setPath(file.getPath());
        fileDirectNode.setPatch(true);
        fileDirectNode.setFile(file);
        return fileDirectNode;
    }
}
