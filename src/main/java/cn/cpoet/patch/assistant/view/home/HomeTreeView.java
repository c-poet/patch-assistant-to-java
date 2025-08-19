package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.control.tree.AppTreeView;
import cn.cpoet.patch.assistant.control.tree.PatchTreeView;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.view.content.ContentSupports;
import cn.cpoet.patch.assistant.view.content.parser.ContentParser;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author CPoet
 */
public abstract class HomeTreeView {

    /**
     * 选中联动标记，避免重复进入
     */
    private static final ThreadLocal<Boolean> SELECTED_LINK_FLAG_TL = new ThreadLocal<>();

    protected final Stage stage;
    protected final HomeContext context;
    protected final AppTreeView appTree;
    protected final PatchTreeView patchTree;

    protected HomeTreeView(Stage stage, HomeContext context) {
        this.stage = stage;
        this.context = context;
        this.appTree = context.getAppTree();
        this.patchTree = context.getPatchTree();
    }

    protected void selectedLink(TreeView<TreeNode> originTree, TreeView<TreeNode> targetTree) {
        if (!Boolean.TRUE.equals(Configuration.getInstance().getIsSelectedLinked())) {
            return;
        }
        if (Boolean.TRUE.equals(SELECTED_LINK_FLAG_TL.get())) {
            return;
        }
        try {
            SELECTED_LINK_FLAG_TL.set(Boolean.TRUE);
            TreeItem<TreeNode> originItem = originTree.getSelectionModel().getSelectedItem();
            if (originItem == null || originItem.getValue() == null) {
                return;
            }
            TreeNode originNode = originItem.getValue();
            if (originNode.getMappedNode() == null) {
                return;
            }
            TreeItem<TreeNode> targetItem = originNode.getMappedNode().getTreeItem();
            MultipleSelectionModel<TreeItem<TreeNode>> selectionModel = targetTree.getSelectionModel();
            selectionModel.clearSelection();
            selectionModel.select(targetItem);
            int targetItemIndex = targetTree.getRow(targetItem);
            targetTree.scrollTo(targetItemIndex);
        } finally {
            SELECTED_LINK_FLAG_TL.remove();
        }
    }

    protected void doSaveFile(TreeNode node, byte[] content, String ext) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18nUtil.t("app.view.tree.save-file"));
        String name = FileNameUtil.getName(FileNameUtil.getFileName(node.getText()));
        if (ext == null) {
            fileChooser.setInitialFileName(name);
        } else {
            fileChooser.setInitialFileName(name + FileNameUtil.C_EXT_SEPARATOR + ext);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext.toUpperCase() + " " + I18nUtil.t("app.view.tree.file"), "*." + ext));
        }
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        FileUtil.writeFile(file, content);
    }

    protected void saveFile(TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        TreeNode node = selectedItem.getValue();
        doSaveFile(node, node.getBytes(), FileNameUtil.getExt(node.getText()));
    }

    protected void saveSourceFile(TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || !selectedItem.getValue().getText().endsWith(FileExtConst.DOT_CLASS)) {
            return;
        }
        TreeNode node = selectedItem.getValue();
        ContentParser parser = ContentSupports.getContentParser(node);
        String content = parser.parse(node);
        doSaveFile(node, content.getBytes(), FileExtConst.JAVA);
    }

    protected boolean isDragFromTreeCell(DragEvent event) {
        return event.getGestureSource() instanceof TreeCell;
    }

    /**
     * 隐藏菜单项
     *
     * @param menu   菜单
     * @param filter 过滤器
     */
    protected void hideMenItem(ContextMenu menu, Predicate<MenuItem> filter) {
        menu.getItems().forEach(item -> {
            if (filter == null || !filter.test(item)) {
                item.setVisible(false);
            }
        });
    }
}
