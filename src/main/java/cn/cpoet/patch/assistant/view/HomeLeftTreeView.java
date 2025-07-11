package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.AppPackService;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.tree.*;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
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

    private void buildAppTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem manualDelMenuItem = new MenuItem("删除");
        manualDelMenuItem.setOnAction(e -> {
            List<TreeNode> treeNodes = appTree.getSelectionModel().getSelectedItems().stream()
                    .map(TreeItem::getValue)
                    .collect(Collectors.toList());
            treeNodes.forEach(node -> {
                TreeNodeUtil.removeNodeChild(node);
                TreeNodeUtil.countNodeStatus(context.totalInfo, node, TreeNodeStatus.MANUAL_DEL);
            });
        });
        MenuItem saveFileMenuItem = new MenuItem("保存文件");
        saveFileMenuItem.setOnAction(e -> saveFile(appTree));
        MenuItem saveSourceFileMenuItem = new MenuItem("保存源文件");
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(appTree));
        contextMenu.getItems().addAll(manualDelMenuItem, saveFileMenuItem, saveSourceFileMenuItem);
        contextMenu.setOnShowing(e -> {
            ObservableList<TreeItem<TreeNode>> selectedItems = appTree.getSelectionModel().getSelectedItems();
            boolean isNoneNode = selectedItems.stream().anyMatch(item -> item.equals(appTree.getRoot()) || item.getValue().getStatus() != TreeNodeStatus.NONE);
            manualDelMenuItem.setVisible(!isNoneNode);
            if (selectedItems.size() != 1 || selectedItems.get(0).getValue().isDir()) {
                saveFileMenuItem.setVisible(false);
                saveSourceFileMenuItem.setVisible(false);
            } else {
                saveFileMenuItem.setVisible(true);
                saveSourceFileMenuItem.setVisible(selectedItems.get(0).getValue().getText().endsWith(FileExtConst.DOT_CLASS));
            }
        });
        appTree.setContextMenu(contextMenu);
    }

    private void refreshAppTree(File file) {
        AppTreeInfo appTreeInfo = AppPackService.getInstance().getTreeNode(file);
        appTree.setTreeInfo(appTreeInfo);
        refreshAppTree(true);
    }

    private void refreshAppTree(boolean isEmitEvent) {
        if (isEmitEvent) {
            appTree.fireEvent(new Event(AppTreeView.APP_TREE_REFRESHING));
        }
        TreeItem<TreeNode> rootItem = appTree.getRoot();
        if (rootItem == null) {
            rootItem = new TreeItem<>();
            appTree.setRoot(rootItem);
        } else {
            rootItem.getChildren().clear();
        }
        AppTreeInfo treeInfo = context.getAppTree().getTreeInfo();
        if (treeInfo != null) {
            TreeNodeUtil.buildNode(rootItem, treeInfo.getRootNode(), OnlyChangeFilter.INSTANCE);
        }
        if (isEmitEvent) {
            appTree.fireEvent(new Event(AppTreeView.APP_TREE_REFRESH));
        }
    }

    private void initAppTreeDrag() {
        appTree.setOnDragOver(e -> {
            if (isDragFromTree(e)) {
                return;
            }
            List<File> files = e.getDragboard().getFiles();
            if (files.size() == 1 && files.get(0).getName().endsWith(FileExtConst.DOT_JAR)) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            e.consume();
        });
        appTree.setOnDragDropped(e -> {
            List<File> files = e.getDragboard().getFiles();
            refreshAppTree(files.get(0));
        });
    }

    private void buildAppTree() {
        appTree.setCellFactory(treeView -> new FileTreeCell(context));
        buildAppTreeContextMenu();
        context.patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESH, e -> refreshAppTree(false));
        appTree.addEventHandler(AppTreeView.ONLY_CHANGE_FILTER_CALL, e -> refreshAppTree(false));
        appTree.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, oldVal, newVal) -> selectedLink(appTree, context.patchTree));
        appTree.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TreeItem<TreeNode> selectedItem = appTree.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    TreeNode selectedTreeNode = selectedItem.getValue();
                    if (selectedTreeNode.getText().endsWith(FileExtConst.DOT_JAR)) {
                        if (AppPackService.getInstance().buildNodeChildrenWithZip(selectedTreeNode, false)) {
                            TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode, OnlyChangeFilter.INSTANCE);
                        }
                        return;
                    }
                    new ContentView(selectedTreeNode).showDialog(stage);
                }
            }
        });
        initAppTreeDrag();
        String lastAppPackPath = Configuration.getInstance().getLastAppPackPath();
        if (lastAppPackPath == null || lastAppPackPath.isBlank()) {
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
        appPackPathBox.getChildren().add(new Label("应用包:"));
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
            node.textProperty().addListener((observableValue, oldVal, newVal) -> {
                Configuration.getInstance().setLastAppPackPath(newVal);
            });
        }));
        appPackPathBox.getChildren().add(FXUtil.pre(new Button("选择"), node -> {
            node.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("选择应用包");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("应用包", "*.jar"));
                File file = fileChooser.showOpenDialog(stage);
                if (file == null) {
                    return;
                }
                refreshAppTree(file);
            });
        }));
        buildAppTree();
        VBox.setVgrow(appTree, Priority.ALWAYS);
        return new VBox(appPackPathBox, appTree);
    }
}
