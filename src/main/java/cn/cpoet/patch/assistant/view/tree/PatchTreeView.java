package cn.cpoet.patch.assistant.view.tree;

import javafx.event.EventType;

/**
 * @author CPoet
 */
public class PatchTreeView extends CustomTreeView<PatchTreeInfo> {
    public static final EventType<?> PATCH_TREE_REFRESH = new EventType<>("PATCH_TREE_REFRESH");
    public static final EventType<?> PATCH_TREE_REFRESHING = new EventType<>("PATCH_TREE_REFRESHING");
}
