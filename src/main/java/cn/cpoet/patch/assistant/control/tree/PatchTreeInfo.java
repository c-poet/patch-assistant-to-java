package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 补丁包信息
 *
 * @author CPoet
 */
public class PatchTreeInfo extends TreeInfo {

    /**
     * 根节点信息
     */
    private PatchRootInfo rootInfo;

    /**
     * 标记根节点信息
     */
    private Map<TreeNode, PatchRootInfo> customRootInfoMap;

    /**
     * readme信息
     */
    private final ReadOnlyStringProperty readmeText = new SimpleStringProperty();

    /**
     * 获取根节点信息
     *
     * @return 根节点信息
     */
    public PatchRootInfo getRootInfo() {
        return rootInfo;
    }

    /**
     * 设置根节点信息
     *
     * @param rootInfo 根节点信息
     */
    public void setRootInfo(PatchRootInfo rootInfo) {
        this.rootInfo = rootInfo;
    }

    /**
     * 根据节点信息获取根信息
     *
     * @param node 节点
     * @return 根节点信息
     */
    public PatchRootInfo getRootInfoByNode(TreeNode node) {
        if (node == getRootNode()) {
            return rootInfo;
        }
        return customRootInfoMap == null ? null : customRootInfoMap.get(node);
    }

    /**
     * 获取根节点绑定信息
     *
     * @return 根节点绑定信息
     */
    public Map<TreeNode, PatchRootInfo> getCustomRootInfoMap() {
        return customRootInfoMap == null ? Collections.emptyMap() : Collections.unmodifiableMap(customRootInfoMap);
    }

    /**
     * 增加补丁根绑定信息
     *
     * @param treeNode      节点
     * @param patchRootInfo 补丁根绑定信息
     */
    public void addCustomRootInfo(TreeNode treeNode, PatchRootInfo patchRootInfo) {
        if (customRootInfoMap == null) {
            customRootInfoMap = new LinkedHashMap<>();
        }
        customRootInfoMap.put(treeNode, patchRootInfo);
        updateReadmeText();
    }

    /**
     * 移出根节点绑定信息
     *
     * @param treeNode 根节点
     */
    public void removeCustomRootInfo(TreeNode treeNode) {
        if (customRootInfoMap != null) {
            customRootInfoMap.remove(treeNode);
        }
        updateReadmeText();
    }

    /**
     * 获取readme内容
     *
     * @return readme内容
     */
    public String getReadmeText() {
        return readmeText.get();
    }

    /**
     * 获取readme内容
     *
     * @return readme内容
     */
    public ReadOnlyStringProperty readmeTextProperty() {
        return readmeText;
    }

    /**
     * 更新说明文件信息
     */
    public void updateReadmeText() {
        String readme = createReadmeText();
        ((StringProperty) readmeText).set(readme);
    }

    private String createReadmeText() {
        if (CollectionUtil.isNotEmpty(customRootInfoMap)) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<TreeNode, PatchRootInfo> entry : customRootInfoMap.entrySet()) {
                TreeNode treeNode = entry.getKey();
                PatchRootInfo patchRootInfo = entry.getValue();
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append(treeNode.getName()).append("\n---");
                String readme = patchRootInfo.getPatchSign().getReadme();
                if (!StringUtil.isBlank(readme)) {
                    sb.append('\n').append(readme);
                }
            }
            return sb.toString();
        }
        String readme = rootInfo.getPatchSign().getReadme();
        return StringUtil.isBlank(readme) ? "" : readme;
    }
}
