package cn.cpoet.patch.assistant.view.tree;

import javafx.event.EventType;

/**
 * @author CPoet
 */
public class AppTreeView extends CustomTreeView<AppTreeInfo> {
    public static final EventType<?> APP_TREE_REFRESH = new EventType<>("APP_TREE_REFRESH");
    public static final EventType<?> APP_TREE_REFRESHING = new EventType<>("APP_TREE_REFRESHING");
    public static final EventType<?> ONLY_CHANGE_FILTER_CALL = new EventType<>("ONLY_CHANGE_FILTER_CALL");
}
