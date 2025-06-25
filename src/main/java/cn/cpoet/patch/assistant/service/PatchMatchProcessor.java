package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.view.tree.TotalInfo;
import cn.cpoet.patch.assistant.view.tree.TreeNode;

import java.util.List;

/**
 * @author CPoet
 */
public class PatchMatchProcessor extends BasePatchMatchProcessor {

    /**
     * 应用包根节点
     */
    private TreeNode appRootNode;

    /**
     * 补丁包根节点
     */
    private TreeNode patchRootNode;

    public PatchMatchProcessor(TotalInfo totalInfo, boolean isWithPath, boolean isWithName) {
        super(totalInfo, isWithPath, isWithName);
    }

    public void setAppRootNode(TreeNode appRootNode) {
        this.appRootNode = appRootNode;
    }

    public void setPatchRootNode(TreeNode patchRootNode) {
        this.patchRootNode = patchRootNode;
    }

    private boolean checkRootNode() {
        return appRootNode != null && CollectionUtil.isNotEmpty(appRootNode.getChildren())
                && patchRootNode != null && CollectionUtil.isNotEmpty(patchRootNode.getChildren());
    }

    @Override
    public void exec() {
        if (!checkRootNode()) {
            return;
        }
        super.exec();
    }

    @Override
    protected List<TreeNode> getAppNodes() {
        return appRootNode.getChildren();
    }

    @Override
    protected List<TreeNode> getPatchNodes() {
        return patchRootNode.getChildren();
    }
}
