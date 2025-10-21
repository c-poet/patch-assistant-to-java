package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.model.AppPackSign;
import cn.cpoet.patch.assistant.model.PatchUpSign;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.JsonUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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

    /**
     * 补丁比较信息
     */
    private final StringProperty patchDiffInfo = new SimpleStringProperty();

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

    public String getPatchDiffInfo() {
        return patchDiffInfo.get();
    }

    public StringProperty patchDiffInfoProperty() {
        return patchDiffInfo;
    }

    public void setPatchDiffInfo(String patchDiffInfo) {
        this.patchDiffInfo.set(patchDiffInfo);
    }

    public void appendPatchDiffInfo(String patchDiffInfo) {
        String s = this.patchDiffInfo.get();
        if (StringUtil.isBlank(s)) {
            this.patchDiffInfo.set(patchDiffInfo);
        } else {
            this.patchDiffInfo.set(s + '\n' + patchDiffInfo);
        }
    }

    public List<PatchUpSign> listPatchUpSign() {
        if (patchUpSignNode == null) {
            return null;
        }
        byte[] bytes = TreeNodeUtil.readNodeBytes(patchUpSignNode);
        if (bytes.length > 0) {
            try {
                JsonUtil.read(bytes, new TypeReference<>() {
                });
            } catch (Exception ignored) {
            }
        }
        return null;
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
