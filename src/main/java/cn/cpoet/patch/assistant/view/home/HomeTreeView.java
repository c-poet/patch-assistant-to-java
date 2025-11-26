package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.common.InputBufConsumer;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.control.menu.MenuItemClaim;
import cn.cpoet.patch.assistant.control.tree.AppTreeView;
import cn.cpoet.patch.assistant.control.tree.CustomTreeView;
import cn.cpoet.patch.assistant.control.tree.PatchTreeView;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.content.ContentSupports;
import cn.cpoet.patch.assistant.view.content.parser.ContentParser;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

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
    /** 右键菜单项前置操作列表 */
    private List<MenuItemClaim> menuItemClaims;
    /** 快速搜索控件 */
    protected final FastSearchControl fastSearchControl;
    /** 加载中标识 */
    protected final BooleanProperty loadingFlag = new SimpleBooleanProperty(false);

    protected HomeTreeView(Stage stage, HomeContext context, FastSearchControl fastSearchControl) {
        this.stage = stage;
        this.context = context;
        this.appTree = context.getAppTree();
        this.patchTree = context.getPatchTree();
        this.fastSearchControl = fastSearchControl;
    }

    public boolean isLoadingFlag() {
        return loadingFlag.get();
    }

    public BooleanProperty loadingFlagProperty() {
        return loadingFlag;
    }

    public void setLoadingFlag(boolean loadingFlag) {
        this.loadingFlag.set(loadingFlag);
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

    protected void doSaveFile(TreeNode node, Consumer<InputBufConsumer> consumer, String ext) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18nUtil.t("app.view.tree.save-file"));
        String name = FileNameUtil.getName(FileNameUtil.getFileName(node.getName()));
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
        FileUtil.writeFile(file, consumer);
    }

    protected void saveFile(TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        TreeNode node = selectedItem.getValue();
        doSaveFile(node, node::consumeBytes, FileNameUtil.getExt(node.getName()));
    }

    protected void saveSourceFile(TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || !selectedItem.getValue().getName().endsWith(FileExtConst.DOT_CLASS)) {
            return;
        }
        TreeNode node = selectedItem.getValue();
        ContentParser parser = ContentSupports.getContentParser(node);
        String content = parser.parse(node);
        doSaveFile(node, consumer -> {
            try {
                byte[] bytes = StringUtil.isEmpty(content) ? new byte[0] : content.getBytes();
                consumer.accept(bytes.length, bytes);
            } catch (Exception e) {
                throw new AppException("Writing to the source file failed", e);
            }
        }, FileExtConst.JAVA);
    }

    protected boolean isDragFromTreeCell(DragEvent event) {
        return event.getGestureSource() instanceof TreeCell;
    }

    /**
     * 处理Enter事件
     *
     * @param event 事件
     */
    protected void handleEnterKey(KeyEvent event, TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() != null) {
            TreeNode selectedNode = selectedItem.getValue();
            if (CollectionUtil.isNotEmpty(selectedNode.getChildren())) {
                selectedItem.setExpanded(!selectedItem.isExpanded());
            }
        }
        event.consume();
    }

    /**
     * 在资源管理器中打开
     *
     * @param event    事件
     * @param treeView 树
     */
    protected void handleOpenInExplorer(ActionEvent event, TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() != null) {
            TreeNode selectedNode = selectedItem.getValue();
            if (selectedNode.isDir()) {
                FileUtil.openFolder(selectedNode.getPath());
            } else {
                FileUtil.selectFile(selectedNode.getPath());
            }
        }
        event.consume();
    }

    protected void addContextMenuItemClaim(MenuItemClaim menuItemClaim) {
        if (menuItemClaims == null) {
            menuItemClaims = new LinkedList<>();
        }
        menuItemClaims.add(menuItemClaim);
    }

    protected void bindContextMenu(CustomTreeView<?> treeView) {
        ContextMenu contextMenu = new ContextMenu();
        treeView.setOnContextMenuRequested(e -> {
            contextMenu.getItems().clear();
            if (CollectionUtil.isEmpty(menuItemClaims)) {
                return;
            }
            menuItemClaims.forEach(menuItemClaim -> {
                if (menuItemClaim.isAccept()) {
                    contextMenu.getItems().add(menuItemClaim.getItem());
                }
            });
        });
        treeView.setContextMenu(contextMenu);
    }
}
