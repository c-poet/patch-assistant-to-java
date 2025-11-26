package cn.cpoet.patch.assistant.control.tree;

import javafx.event.EventType;

/**
 * 补丁树视图
 *
 * @author CPoet
 */
public class PatchTreeView extends CustomTreeView<PatchTreeInfo> {
    /**
     * 补丁树刷新事件
     */
    public static final EventType<?> PATCH_TREE_REFRESH = new EventType<>("PATCH_TREE_REFRESH");
    /**
     * 补丁树刷新前事件
     */
    public static final EventType<?> PATCH_TREE_REFRESHING = new EventType<>("PATCH_TREE_REFRESHING");
    /**
     * 标记根节点变化事件
     */
    public static final EventType<PatchMarkRootEvent> PATCH_MARK_ROOT_CHANGE = new EventType<>("PATCH_ADD_MARK_ROOT");

    /**
     * 补丁树刷新事件
     */
    public static final int REFRESH_FLAG_NONE = 0;

    /**
     * 补丁树刷新是否需要发布事件
     */
    public static final int REFRESH_FLAG_EMIT_EVENT = 1;

    /**
     * 补丁树刷新是否更新构建树
     */
    public static final int REFRESH_FLAG_BUILD_TREE_ITEM = 1 << 1;
}
