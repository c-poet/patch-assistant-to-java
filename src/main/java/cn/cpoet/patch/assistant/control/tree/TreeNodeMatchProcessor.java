package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.MappedNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.control.tree.node.VirtualNode;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.util.UIUtil;

import java.util.List;

/**
 * @author CPoet
 */
public abstract class TreeNodeMatchProcessor<E> {

    protected final TreeNode appNode;
    protected final TotalInfo totalInfo;
    protected final List<E> patchElements;

    public TreeNodeMatchProcessor(TotalInfo totalInfo, TreeNode appNode, List<E> patchElements) {
        this.totalInfo = totalInfo;
        this.appNode = appNode;
        this.patchElements = patchElements;
    }

    public void exec() {
        if (CollectionUtil.isEmpty(patchElements)) {
            return;
        }
        if (patchElements.size() == 1 && isDirectory(patchElements.get(0)) && appNode.isDir()) {
            matchWithDir(patchElements.get(0), appNode);
            return;
        }
        TreeNode tarAppNode = appNode.isDir() ? appNode : appNode.getParent();
        patchElements.forEach(element -> match(element, tarAppNode));
    }

    protected abstract boolean isDirectory(E patchElement);

    protected abstract List<E> listChildren(E patchElement);

    protected abstract boolean isMatch(TreeNode appNode, E patchElement);

    protected abstract TreeNode getElementNode(E patchElement);

    private void matchWithDir(E patchElement, TreeNode appNode) {
        List<E> elements = listChildren(patchElement);
        if (CollectionUtil.isEmpty(elements)) {
            return;
        }
        elements.forEach(element -> match(element, appNode));
    }

    private void match(E patchElement, TreeNode appNode) {
        if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
            for (TreeNode childNode : appNode.getChildren()) {
                if (isMatch(childNode, patchElement)) {
                    matchMapping(childNode, patchElement);
                    return;
                }
            }
        }
        createAppItem(patchElement, appNode);
    }

    private void matchMapping(TreeNode appNode, E patchElement) {
        if (appNode instanceof VirtualNode || appNode instanceof MappedNode) {
            TreeNodeUtil.removeNodeChild(appNode);
            createAppItem(patchElement, appNode);
            return;
        }
        TreeNode patchNode = getElementNode(patchElement);
        TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.MOD);
        matchWithDir(patchElement, appNode);
    }

    private void createAppItem(E patchElement, TreeNode appNode) {
        TreeNode patchNode = getElementNode(patchElement);
        TreeNode childNode = createAndGetAppNode(patchNode, appNode);
        List<E> elements = listChildren(patchElement);
        if (CollectionUtil.isNotEmpty(elements)) {
            elements.forEach(element -> match(element, childNode));
        }
    }

    protected TreeNode createAndGetAppNode(TreeNode patchNode, TreeNode appNode) {
        TreeNode childNode;
        if (patchNode.isDir()) {
            VirtualNode virtualNode = new VirtualNode();
            virtualNode.setName(patchNode.getName());
            virtualNode.setModifyTime(patchNode.getModifyTime());
            virtualNode.setDir(true);
            childNode = virtualNode;
        } else {
            childNode = new MappedNode(patchNode);
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
        UIUtil.runUI(() -> appNode.getTreeItem().getChildren().add(childItem));
        if (!patchNode.isDir()) {
            TreeNodeUtil.mappedNode(totalInfo, childNode, patchNode, TreeNodeType.ADD);
        }
        return childNode;
    }
}
