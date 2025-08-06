package cn.cpoet.patch.assistant.view.tree;

import javafx.event.EventType;

/**
 * @author CPoet
 */
public class AppTreeView extends CustomTreeView<AppTreeInfo> {
    /**
     * 应用树刷新事件
     */
    public static final EventType<?> APP_TREE_REFRESH = new EventType<>("APP_TREE_REFRESH");
    /**
     * 应用树刷新前事件
     */
    public static final EventType<?> APP_TREE_REFRESHING = new EventType<>("APP_TREE_REFRESHING");
    /**
     * 请求应用树普通刷新
     */
    public static final EventType<?> APP_TREE_NONE_REFRESH_CALL = new EventType<>("APP_TREE_NONE_REFRESH_CALL");

    /**
     * 补丁树刷新事件
     */
    public static final int REFRESH_FLAG_NONE = 0;

    /**
     * 补丁树刷新是否需要发布事件
     */
    public static final int REFRESH_FLAG_EMIT_EVENT = 1;

    public AppTreeView() {
        super();
        setEditable(true);
    }
}
