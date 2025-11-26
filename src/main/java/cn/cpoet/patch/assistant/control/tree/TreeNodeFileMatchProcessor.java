package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.FileDirectNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.service.PatchPackService;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 树形节点和文件匹配
 *
 * @author CPoet
 */
public class TreeNodeFileMatchProcessor extends TreeNodeMatchProcessor<File> {

    public TreeNodeFileMatchProcessor(TotalInfo totalInfo, AppTreeInfo appTreeInfo, TreeNode appNode, List<File> files) {
        super(totalInfo, appTreeInfo, appNode, files);
    }

    @Override
    protected boolean isDirectory(File patchElement) {
        return patchElement.isDirectory();
    }

    @Override
    protected List<File> listChildren(File patchElement) {
        if (patchElement.isFile()) {
            return Collections.emptyList();
        }
        File[] files = patchElement.listFiles();
        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }

    @Override
    protected boolean isMatch(TreeNode appNode, File patchElement) {
        return PatchPackService.INSTANCE.matchPatchName(appNode, patchElement.getName());
    }

    @Override
    protected TreeNode getElementNode(File patchElement) {
        return createNode(patchElement);
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
