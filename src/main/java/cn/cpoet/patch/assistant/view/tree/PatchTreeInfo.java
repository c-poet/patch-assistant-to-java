package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 补丁包信息
 *
 * @author CPoet
 */
public class PatchTreeInfo extends TreeInfo<PatchSignTreeNode> {

    /**
     * 标记根节点信息
     */
    private List<PatchSignTreeNode> markRootNodes;

    public List<PatchSignTreeNode> getMarkRootNodes() {
        return markRootNodes;
    }

    public List<PatchSignTreeNode> getAndInitMarkRootNodes() {
        if (markRootNodes == null) {
            markRootNodes = new ArrayList<>();
        }
        return markRootNodes;
    }

    public void setMarkRootNodes(List<PatchSignTreeNode> markRootNodes) {
        this.markRootNodes = markRootNodes;
    }

    /**
     * 读取ReadMe文本内容
     *
     * @return Read文本内容
     */
    public String getReadMeText() {
        if (CollectionUtil.isNotEmpty(markRootNodes)) {
            StringBuilder sb = new StringBuilder();
            for (PatchSignTreeNode markRootNode : markRootNodes) {
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append(markRootNode.getName()).append("\n---\n").append(markRootNode.getPatchSign().getReadme());
            }
            return sb.toString();
        }
        return getRootNode().getPatchSign().getReadme();
    }
}
