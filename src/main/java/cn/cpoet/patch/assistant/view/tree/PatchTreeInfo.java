package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.model.PatchSign;
import cn.cpoet.patch.assistant.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 补丁包信息
 *
 * @author CPoet
 */
public class PatchTreeInfo extends TreeInfo {

    /**
     * 补丁签名
     */
    private PatchSign patchSign;

    /**
     * 自定义根节点
     */
    private List<TreeNode> markRootNodes;

    /**
     * 标记根节点的补丁签名信息
     */
    private List<PatchSign> markPatchSigns;

    public PatchSign getPatchSign() {
        return patchSign;
    }

    public void setPatchSign(PatchSign patchSign) {
        this.patchSign = patchSign;
    }

    public List<TreeNode> getMarkRootNodes() {
        return markRootNodes;
    }

    public List<TreeNode> getCurRootNodes() {
        return CollectionUtil.isEmpty(markRootNodes) ? Collections.singletonList(getRootNode()) : markRootNodes;
    }

    public void setMarkRootNodes(List<TreeNode> markRootNodes) {
        this.markRootNodes = markRootNodes;
    }

    public void addMarkRootNode(TreeNode node) {
        if (markRootNodes == null) {
            markRootNodes = new ArrayList<>();
        }
        markRootNodes.add(node);
    }

    public List<PatchSign> getMarkPatchSigns() {
        return markPatchSigns;
    }

    public void setMarkPatchSigns(List<PatchSign> markPatchSigns) {
        this.markPatchSigns = markPatchSigns;
    }

    public void addMarkPatchSign(PatchSign sign) {
        if (markPatchSigns == null) {
            markPatchSigns = new ArrayList<>();
        }
        markPatchSigns.add(sign);
    }

    /**
     * 读取ReadMe文本内容
     *
     * @return Read文本内容
     */
    public String getReadMeText() {
        return patchSign.getReadme();
    }
}
