package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.AppPackService;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.tree.FileTreeCell;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.TreeNodeStatus;
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
        MenuItem markDelMenuItem = new MenuItem();
        markDelMenuItem.setOnAction(e -> {
            TreeItem<TreeNode> selectedItem = context.appTree.getSelectionModel().getSelectedItem();
            TreeKindNode selectedNode = (TreeKindNode) selectedItem.getValue();
            selectedNode.setStatus(selectedNode.getStatus() == TreeNodeStatus.NONE ? TreeNodeStatus.MARK_DEL : TreeNodeStatus.NONE);
        });
        MenuItem saveFileMenuItem = new MenuItem("保存文件");
        saveFileMenuItem.setOnAction(e -> saveFile(context.appTree));
        MenuItem saveSourceFileMenuItem = new MenuItem("保存源文件");
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(context.appTree));
        contextMenu.getItems().addAll(markDelMenuItem, saveFileMenuItem, saveSourceFileMenuItem);
        contextMenu.setOnShowing(e -> {
            TreeItem<TreeNode> selectedItem = context.appTree.getSelectionModel().getSelectedItem();
            TreeNode selectedNode = selectedItem.getValue();
            if (selectedNode instanceof TreeKindNode) {
                TreeNodeStatus status = ((TreeKindNode) selectedNode).getStatus();
                if (status == TreeNodeStatus.NONE) {
                    markDelMenuItem.setText("标记删除");
                    markDelMenuItem.setVisible(true);
                } else if (status == TreeNodeStatus.MARK_DEL) {
                    markDelMenuItem.setText("取消标记删除");
                    markDelMenuItem.setVisible(true);
                } else {
                    markDelMenuItem.setVisible(false);
                }
                saveFileMenuItem.setVisible(true);
                saveSourceFileMenuItem.setVisible(selectedNode.getName().endsWith(FileExtConst.DOT_CLASS));
            } else {
                markDelMenuItem.setVisible(false);
                saveFileMenuItem.setVisible(false);
                saveSourceFileMenuItem.setVisible(false);
            }
        });
        context.appTree.setContextMenu(contextMenu);
    }

    protected void refreshAppTree(File file) {
        context.appTreeInfo = AppPackService.getInstance().getTreeNode(file);
        TreeItem<TreeNode> rootItem = context.appTree.getRoot();
        if (rootItem == null) {
            rootItem = new TreeItem<>();
            context.appTree.setRoot(rootItem);
        } else {
            rootItem.getChildren().clear();
        }
        TreeNodeUtil.buildNode(rootItem, context.appTreeInfo.getRootNode(), OnlyChangeFilter.INSTANCE);
        PatchPackService.getInstance().refreshPatchMappedNode(context.totalInfo, context.appTreeInfo, context.patchTreeInfo);
        context.appPathTextField.setText(file.getPath());
        Configuration.getInstance().setLastAppPackPath(file.getPath());
    }

    protected void setAppTreeDrag() {
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
        context.appTree = new TreeView<>();
        context.appTree.setCellFactory(treeView -> new FileTreeCell(context));
        buildAppTreeContextMenu();
        context.appTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            selectedLink(context.appTree, context.patchTree);
        });
        context.appTree.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TreeItem<TreeNode> selectedItem = context.appTree.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    TreeNode selectedTreeNode = selectedItem.getValue();
                    if (selectedTreeNode.getName().endsWith(FileExtConst.DOT_JAR)) {
                        if (AppPackService.getInstance().buildNodeChildrenWithZip(selectedTreeNode, false)) {
                            TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode, OnlyChangeFilter.INSTANCE);
                        }
                    }
                    new ContentView().showDialog(stage, (TreeKindNode) selectedTreeNode);
                }
            }
        });
        setAppTreeDrag();
        String lastAppPackPath = Configuration.getInstance().getLastAppPackPath();
        if (lastAppPackPath == null || lastAppPackPath.isBlank()) {
            return;
        }
        File file = FileUtil.getExistsFile(lastAppPackPath);
        if (file == null) {
            return;
        }
        refreshAppTree(file);
    }

    protected Node build() {
        HBox appPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        appPackPathBox.getChildren().add(new Label("应用包:"));
        appPackPathBox.getChildren().add(FXUtil.pre(context.appPathTextField = new TextField(), node -> {
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
        }));
        appPackPathBox.getChildren().add(FXUtil.pre(new Button("选择"), node -> {
            node.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                if (!context.appPathTextField.getText().isBlank()) {
                    fileChooser.setInitialFileName(context.appPathTextField.getText());
                }
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
