package cn.cpoet.patch.assistant.control.tree;

/**
 * 节点类型枚举
 *
 * @author CPoet
 */
public enum TreeNodeType {
    /**
     * 新增节点
     */
    ADD,

    /**
     * 删除节点
     */
    DEL,

    /**
     * 修改节点
     */
    MOD,

    /**
     * 手动删除节点
     */
    MANUAL_DEL,

    /**
     * Readme节点
     */
    README,

    /**
     * 根节点
     */
    ROOT,

    /**
     * 自定义根节点
     */
    CUSTOM_ROOT,

    /**
     * 普通节点
     */
    NONE
}
