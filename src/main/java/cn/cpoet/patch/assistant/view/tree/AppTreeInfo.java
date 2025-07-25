package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.model.AppPackSign;
import cn.cpoet.patch.assistant.model.PatchUpSign;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.JsonUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Set<String> getAllPatchUpSignSha1() {
        List<PatchUpSign> patchUpSigns = listPatchUpSign();
        if (CollectionUtil.isEmpty(patchUpSigns)) {
            return Collections.emptySet();
        }
        Set<String> sha1Set = new HashSet<>();
        for (PatchUpSign patchUpSign : patchUpSigns) {
            if (!StringUtil.isBlank(patchUpSign.getSha1())) {
                sha1Set.add(patchUpSign.getSha1());
            }
            if (CollectionUtil.isNotEmpty(patchUpSign.getSigns())) {
                patchUpSign.getSigns().forEach(sign -> {
                    if (!StringUtil.isBlank(sign.getSha1())) {
                        sha1Set.add(sign.getSha1());
                    }
                });
            }
        }
        return sha1Set;
    }
}
