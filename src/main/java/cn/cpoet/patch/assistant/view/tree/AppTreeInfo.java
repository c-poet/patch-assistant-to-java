package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.model.AppPackSign;
import cn.cpoet.patch.assistant.model.PatchUpSign;
import cn.cpoet.patch.assistant.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * 应用信息
 *
 * @author CPoet
 */
public class AppTreeInfo extends TreeInfo {

    /**
     * 应用包签名信息
     */
    private AppPackSign appPackSign;

    /**
     * 应用补丁签名节点
     */
    private TreeNode patchUpSignNode;

    public AppPackSign getAppPackSign() {
        return appPackSign;
    }

    public void setAppPackSign(AppPackSign appPackSign) {
        this.appPackSign = appPackSign;
    }

    public TreeNode getPatchUpSignNode() {
        return patchUpSignNode;
    }

    public void setPatchUpSignNode(TreeNode patchUpSignNode) {
        this.patchUpSignNode = patchUpSignNode;
    }

    public List<PatchUpSign> listPatchUpSign() {
        if (patchUpSignNode == null) {
            return null;
        }
        byte[] bytes = patchUpSignNode.getBytes();
        return bytes == null ? null : JsonUtil.read(bytes, new TypeReference<>() {
        });
    }
}
