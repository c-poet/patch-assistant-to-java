package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.AppPackService;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.tree.FileTreeCell;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.TreeNodeStatus;
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

/**
 * @author CPoet
 */
public class HomeLeftTreeView extends HomeTreeView {

    public HomeLeftTreeView(Stage stage, HomeContext context) {
        super(stage, context);
    }

    protected void buildAppTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem manualDelMenuItem = new MenuItem("删除");
        manualDelMenuItem.setOnAction(e -> {
            context.appTree.getSelectionModel().getSelectedItems().forEach(item -> {
                TreeNode selectedNode = item.getValue();
                TreeNode nodeParent = selectedNode.getParent();
                nodeParent.getChildren().remove(selectedNode);
                TreeItem<TreeNode> itemParent = item.getParent();
                itemParent.getChildren().remove(item);
                TreeNodeUtil.countNodeStatus(context.totalInfo, selectedNode, TreeNodeStatus.MANUAL_DEL);
            });
        });
        MenuItem saveFileMenuItem = new MenuItem("保存文件");
        saveFileMenuItem.setOnAction(e -> saveFile(context.appTree));
        MenuItem saveSourceFileMenuItem = new MenuItem("保存源文件");
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(context.appTree));
        contextMenu.getItems().addAll(manualDelMenuItem, saveFileMenuItem, saveSourceFileMenuItem);
        contextMenu.setOnShowing(e -> {
            boolean isNoneNode = context.appTree
                    .getSelectionModel()
                    .getSelectedItems()
                    .stream().anyMatch(item -> item.equals(context.appTree.getRoot()) || item.getValue().getStatus() != TreeNodeStatus.NONE);
            manualDelMenuItem.setVisible(!isNoneNode);
            TreeNode selectedNode = context.appTree.getSelectionModel().getSelectedItem().getValue();
            if (selectedNode.isDir()) {
                saveFileMenuItem.setVisible(false);
                saveSourceFileMenuItem.setVisible(false);
            } else {
                saveFileMenuItem.setVisible(true);
                saveSourceFileMenuItem.setVisible(selectedNode.getText().endsWith(FileExtConst.DOT_CLASS));
            }
        });
        context.appTree.setContextMenu(contextMenu);
    }

    protected void refreshAppTree(File file) {
        context.appTreeInfo = AppPackService.getInstance().getTreeNode(file);
        refreshAppTree(true);
    }

    protected void refreshAppTree(boolean isEmitEvent) {
        if (isEmitEvent) {
            context.appTree.fireEvent(new Event(HomeContext.APP_TREE_REFRESHING));
        }
        TreeItem<TreeNode> rootItem = context.appTree.getRoot();
        if (rootItem == null) {
            rootItem = new TreeItem<>();
            context.appTree.setRoot(rootItem);
        } else {
            rootItem.getChildren().clear();
        }
        TreeNodeUtil.buildNode(rootItem, context.appTreeInfo.getRootNode(), OnlyChangeFilter.INSTANCE);
        if (isEmitEvent) {
            context.appTree.fireEvent(new Event(HomeContext.APP_TREE_REFRESH));
        }
    }

    protected void initAppTreeDrag() {
        context.appTree.setOnDragOver(e -> {
            List<File> files = e.getDragboard().getFiles();
            if (files.size() == 1 && files.get(0).getName().endsWith(FileExtConst.DOT_JAR)) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            e.consume();
        });
        context.appTree.setOnDragDropped(e -> {
            List<File> files = e.getDragboard().getFiles();
            refreshAppTree(files.get(0));
        });
    }

    protected void buildAppTree() {
        context.appTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        context.appTree.setCellFactory(treeView -> new FileTreeCell(context));
        buildAppTreeContextMenu();
        context.patchTree.addEventHandler(HomeContext.PATCH_TREE_REFRESH, e -> refreshAppTree(false));
        context.appTree.addEventHandler(HomeContext.APP_TREE_REFRESH_CALL, e -> refreshAppTree(true));
        context.appTree.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, oldVal, newVal) -> selectedLink(context.appTree, context.patchTree));
        context.appTree.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TreeItem<TreeNode> selectedItem = context.appTree.getSelectionModel().getSelectedItem();
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

    protected Node build() {
        HBox appPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        appPackPathBox.getChildren().add(new Label("应用包:"));
        appPackPathBox.getChildren().add(FXUtil.pre(new TextField(), node -> {
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
            if (context.appTreeInfo != null && context.appTreeInfo.getRootNode() != null) {
                node.setText(context.appTreeInfo.getRootNode().getPath());
            }
            context.appTree.addEventHandler(HomeContext.APP_TREE_REFRESH, e -> {
                if (context.appTreeInfo != null && context.appTreeInfo.getRootNode() != null) {
                    node.setText(context.appTreeInfo.getRootNode().getPath());
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
        VBox.setVgrow(context.appTree, Priority.ALWAYS);
        return new VBox(appPackPathBox, context.appTree);
    }
}
