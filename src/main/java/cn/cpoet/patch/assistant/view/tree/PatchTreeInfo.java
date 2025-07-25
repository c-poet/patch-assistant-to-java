package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.StringUtil;

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
        return customRootInfoMap;
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
    }

    /**
     * 移出根节点绑定信息
     *
     * @param treeNode 根节点
     * @return 根节点绑定信息
     */
    public PatchRootInfo removeCustomRootInfo(TreeNode treeNode) {
        return customRootInfoMap == null ? null : customRootInfoMap.remove(treeNode);
    }

    /**
     * 读取ReadMe文本内容
     *
     * @return Read文本内容
     */
    public String getReadMeText() {
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
