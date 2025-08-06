package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.AppPackService;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.tree.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CPoet
 */
public class HomeLeftTreeView extends HomeTreeView {

    public HomeLeftTreeView(Stage stage, HomeContext context) {
        super(stage, context);
    }

    private void handleManualDel(ActionEvent event) {
        List<TreeNode> treeNodes = appTree.getSelectionModel().getSelectedItems().stream()
                .map(TreeItem::getValue)
                .collect(Collectors.toList());
        treeNodes.forEach(node -> {
            TreeNodeUtil.deepCleanMappedNode(context.totalInfo, node);
            TreeNodeUtil.removeNodeChild(node);
            TreeNodeUtil.countNodeType(context.totalInfo, node, TreeNodeType.MANUAL_DEL);
        });
        patchTree.refresh();
    }

    private void handleMarkDel(ActionEvent event) {
        TreeNode node = appTree.getSelectionModel().getSelectedItem().getValue();
        if (TreeNodeType.DEL.equals(node.getType())) {
            TreeNodeUtil.deepCleanMappedNode(context.totalInfo, node);
            appTree.refresh();
        } else {
            TreeNodeUtil.deepCleanMappedNode(context.totalInfo, node);
            TreeNodeUtil.countAndSetNodeType(context.totalInfo, node, TreeNodeType.DEL);
            appTree.refresh();
            patchTree.refresh();
        }
    }

    private void handleRename(ActionEvent event) {
        TreeItem<TreeNode> selectedItem = appTree.getSelectionModel().getSelectedItem();
        appTree.edit(selectedItem);
    }

    private void buildAppTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem mkdirMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.mkdir"));
        contextMenu.getItems().add(mkdirMenuItem);
        MenuItem renameMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.rename"));
        renameMenuItem.setOnAction(this::handleRename);
        contextMenu.getItems().add(renameMenuItem);
        MenuItem manualDelMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.delete"));
        manualDelMenuItem.setOnAction(this::handleManualDel);
        contextMenu.getItems().add(manualDelMenuItem);
        MenuItem markDelMenuItem = new MenuItem();
        markDelMenuItem.setOnAction(this::handleMarkDel);
        contextMenu.getItems().add(markDelMenuItem);
        MenuItem saveFileMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.save-file"));
        saveFileMenuItem.setOnAction(e -> saveFile(appTree));
        contextMenu.getItems().add(saveFileMenuItem);
        MenuItem saveSourceFileMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.save-source-file"));
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(appTree));
        contextMenu.getItems().add(saveSourceFileMenuItem);

        contextMenu.setOnShowing(e -> {
            ObservableList<TreeItem<TreeNode>> selectedItems = appTree.getSelectionModel().getSelectedItems();
            boolean isNoneNode = selectedItems.stream().anyMatch(item -> item.equals(appTree.getRoot()) || !item.getValue().getType().equals(TreeNodeType.NONE));
            mkdirMenuItem.setVisible(false);
            renameMenuItem.setVisible(false);
            manualDelMenuItem.setVisible(!isNoneNode);
            saveFileMenuItem.setVisible(false);
            saveSourceFileMenuItem.setVisible(false);
            markDelMenuItem.setVisible(false);
            if (selectedItems.size() == 1) {
                TreeNode node = selectedItems.get(0).getValue();
                if (!node.isDir()) {
                    saveFileMenuItem.setVisible(true);
                    saveSourceFileMenuItem.setVisible(node.getText().endsWith(FileExtConst.DOT_CLASS));
                } else {
                    mkdirMenuItem.setVisible(true);
                }
                renameMenuItem.setVisible(true);
                markDelMenuItem.setVisible(true);
                markDelMenuItem.setText(TreeNodeType.DEL.equals(node.getType()) ? I18nUtil.t("app.view.left-tree.unmark-delete") :
                        I18nUtil.t("app.view.left-tree.mark-delete"));
            }
        });
        appTree.setContextMenu(contextMenu);
    }

    private void refreshAppTree(File file) {
        AppTreeInfo appTreeInfo = AppPackService.getInstance().getTreeNode(file);
        appTree.setTreeInfo(appTreeInfo);
        refreshAppTree(AppTreeView.REFRESH_FLAG_EMIT_EVENT);
    }

    private void refreshAppTree(int refreshFlag) {
        if ((refreshFlag & AppTreeView.REFRESH_FLAG_EMIT_EVENT) == AppTreeView.REFRESH_FLAG_EMIT_EVENT) {
            appTree.fireEvent(new Event(AppTreeView.APP_TREE_REFRESHING));
        }
        TreeItem<TreeNode> rootItem = appTree.getRoot();
        if (rootItem == null) {
            rootItem = new TreeItem<>();
            appTree.setRoot(rootItem);
        } else {
            rootItem.getChildren().clear();
        }
        AppTreeInfo treeInfo = appTree.getTreeInfo();
        if (treeInfo != null) {
            TreeNodeUtil.buildNode(rootItem, treeInfo.getRootNode(), OnlyChangeFilter.INSTANCE);
        }
        if ((refreshFlag & AppTreeView.REFRESH_FLAG_EMIT_EVENT) == AppTreeView.REFRESH_FLAG_EMIT_EVENT) {
            appTree.fireEvent(new Event(AppTreeView.APP_TREE_REFRESH));
        }
    }

    private void onDragOver(DragEvent event) {
        if (isDragFromTreeCell(event)) {
            return;
        }
        List<File> files = event.getDragboard().getFiles();
        if (files.size() == 1 && files.get(0).getName().endsWith(FileExtConst.DOT_JAR)) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        refreshAppTree(files.get(0));
    }

    private void initAppTreeDrag() {
        appTree.setOnDragOver(this::onDragOver);
        appTree.setOnDragDropped(this::onDragDropped);
    }

    private void onMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            TreeItem<TreeNode> selectedItem = appTree.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                TreeNode selectedTreeNode = selectedItem.getValue();
                if (selectedTreeNode.getText().endsWith(FileExtConst.DOT_JAR)) {
                    if (AppPackService.getInstance().buildNodeChildrenWithZip(selectedTreeNode, false)) {
                        TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode, OnlyChangeFilter.INSTANCE);
                    }
                } else {
                    new ContentView(selectedTreeNode).showDialog(stage);
                }
            }
        }
    }

    private void buildAppTree() {
        appTree.setCellFactory(treeView -> new EditFileTreeCell(context));
        buildAppTreeContextMenu();
        patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESH, e -> refreshAppTree(AppTreeView.REFRESH_FLAG_NONE));
        appTree.addEventHandler(AppTreeView.APP_TREE_NONE_REFRESH_CALL, e -> refreshAppTree(AppTreeView.REFRESH_FLAG_NONE));
        appTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal)
                -> selectedLink(appTree, patchTree));
        appTree.setOnMouseClicked(this::onMouseClicked);
        initAppTreeDrag();
        String lastAppPackPath = Configuration.getInstance().getLastAppPackPath();
        if (StringUtil.isBlank(lastAppPackPath)) {
            return;
        }
        File file = FileUtil.getExistsFile(lastAppPackPath);
        if (file != null) {
            refreshAppTree(file);
        }
    }

    public Node build() {
        HBox appPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        appPackPathBox.getChildren().add(new Label(I18nUtil.t("app.view.left-tree.app-package")));
        appPackPathBox.getChildren().add(FXUtil.pre(new TextField(), node -> {
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
            AppTreeInfo appTreeInfo = appTree.getTreeInfo();
            if (appTreeInfo != null && appTreeInfo.getRootNode() != null) {
                node.setText(appTreeInfo.getRootNode().getPath());
            }
            appTree.addEventHandler(AppTreeView.APP_TREE_REFRESH, e -> {
                AppTreeInfo treeInfo = appTree.getTreeInfo();
                if (treeInfo != null && treeInfo.getRootNode() != null) {
                    node.setText(treeInfo.getRootNode().getPath());
                }
            });
            node.textProperty().addListener((observableValue, oldVal, newVal) -> Configuration.getInstance().setLastAppPackPath(newVal));
        }));
        appPackPathBox.getChildren().add(FXUtil.pre(new Button(I18nUtil.t("app.view.left-tree.select")), node ->
                node.setOnAction(e -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(I18nUtil.t("app.view.left-tree.select-jar"));
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18nUtil.t("app.view.left-tree.java-package"), "*.jar"));
                    File file = fileChooser.showOpenDialog(stage);
                    if (file == null) {
                        return;
                    }
                    refreshAppTree(file);
                })
        ));
        buildAppTree();
        VBox.setVgrow(appTree, Priority.ALWAYS);
        return new VBox(appPackPathBox, appTree);
    }
}
