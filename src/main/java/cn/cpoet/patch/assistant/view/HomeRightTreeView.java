package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.tree.FileCheckBoxTreeCell;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * @author CPoet
 */
public class HomeRightTreeView extends HomeTreeView {

    public HomeRightTreeView(Stage stage, HomeContext context) {
        super(stage, context);
    }

    protected void buildPatchTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem markRootMenuItem = new MenuItem();
        markRootMenuItem.setOnAction(e -> {
            TreeItem<TreeNode> selectedItem = context.patchTree.getSelectionModel().getSelectedItem();
            TreeNode customRootNode = context.patchTreeInfo.getCustomRootNode();
            if (Objects.equals(customRootNode, selectedItem.getValue())) {
                context.patchTreeInfo.setCustomRootNode(null);
            } else {
                context.patchTreeInfo.setCustomRootNode(selectedItem.getValue());
            }
            PatchPackService patchPackService = PatchPackService.getInstance();
            patchPackService.refreshReadmeNode(context.patchTreeInfo);
            patchPackService.refreshPatchMappedNode(context.totalInfo, context.appTreeInfo, context.patchTreeInfo);
            TreeNodeUtil.expendedMappedOrAllNode(context.totalInfo, context.patchTree.getRoot());
            context.readMeTextArea.setText(context.patchTreeInfo.getReadMeText());
            context.patchTree.refresh();
        });
        contextMenu.getItems().addAll(markRootMenuItem);
        contextMenu.setOnShowing(e -> {
            TreeItem<TreeNode> selectedItem = context.patchTree.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            TreeNode selectedNode = selectedItem.getValue();
            if (selectedNode != context.patchTreeInfo.getRootNode() &&
                    selectedNode.getChildren() != null && !selectedNode.getChildren().isEmpty()) {
                markRootMenuItem.setVisible(true);
                if (Objects.equals(selectedNode, context.patchTreeInfo.getCustomRootNode())) {
                    markRootMenuItem.setText("取消根节点标记");
                } else {
                    markRootMenuItem.setText("标记为根节点");
                }
            } else {
                markRootMenuItem.setVisible(false);
            }
        });
        context.patchTree.setContextMenu(contextMenu);
    }

    protected void refreshPatchTree(File file) {
        PatchPackService patchPackService = PatchPackService.getInstance();
        context.patchTreeInfo = patchPackService.getTreeNode(file);
        patchPackService.refreshReadmeNode(context.patchTreeInfo);
        TreeItem<TreeNode> rootItem = context.patchTree.getRoot();
        if (rootItem == null) {
            rootItem = new CheckBoxTreeItem<>();
            context.patchTree.setRoot(rootItem);
        } else {
            rootItem.getChildren().clear();
        }
        TreeNodeUtil.buildNode(rootItem, context.patchTreeInfo.getRootNode());
        patchPackService.refreshPatchMappedNode(context.totalInfo, context.appTreeInfo, context.patchTreeInfo);
        TreeNodeUtil.expendedMappedOrAllNode(context.totalInfo, rootItem);
        context.patchPathTextField.setText(file.getPath());
        Configuration.getInstance().setLastPatchPackPath(file.getPath());
        if (context.readMeTextArea != null) {
            context.readMeTextArea.setText(context.patchTreeInfo.getReadMeText());
        }
    }

    protected void setPatchTreeDrag() {
        context.patchTree.setOnDragOver(e -> {
            List<File> files = e.getDragboard().getFiles();
            if (files.size() == 1 && (files.get(0).isDirectory() ||
                    files.get(0).getName().endsWith(FileExtConst.DOT_ZIP))) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            e.consume();
        });
        context.patchTree.setOnDragDropped(e -> {
            List<File> files = e.getDragboard().getFiles();
            refreshPatchTree(files.get(0));
        });
    }

    protected void buildPatchTree() {
        context.patchTree = new TreeView<>();
        context.patchTree.setCellFactory(v -> new FileCheckBoxTreeCell(context));
        buildPatchTreeContextMenu();
        context.patchTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            selectedLink(context.patchTree, context.appTree);
        });
        context.patchTree.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TreeItem<TreeNode> selectedItem = context.patchTree.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    TreeNode selectedTreeNode = selectedItem.getValue();

                    if (!selectedTreeNode.getName().endsWith(FileExtConst.DOT_ZIP)) {
                        return;
                    }
                    if (PatchPackService.getInstance().buildNodeChildrenWithZip(selectedTreeNode)) {
                        TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode);
                    }
                }
            }
        });
        setPatchTreeDrag();
        String lastPatchPackPath = Configuration.getInstance().getLastPatchPackPath();
        if (lastPatchPackPath == null || lastPatchPackPath.isBlank()) {
            return;
        }
        File file = FileUtil.getExistsDirOrFile(lastPatchPackPath);
        if (file == null) {
            return;
        }
        refreshPatchTree(file);
    }

    public Node build() {
        HBox patchPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        patchPackPathBox.getChildren().add(new Label("补丁包:"));
        patchPackPathBox.getChildren().add(FXUtil.pre(context.patchPathTextField = new TextField(), node -> {
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
        }));
        patchPackPathBox.getChildren().add(FXUtil.pre(new Button("选择"), node -> {
            node.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                if (!context.patchPathTextField.getText().isBlank()) {
                    fileChooser.setInitialFileName(context.patchPathTextField.getText());
                }
                fileChooser.setTitle("选择补丁包");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("补丁包", "*.zip"));
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    refreshPatchTree(file);
                }
            });
        }));
        patchPackPathBox.getChildren().add(FXUtil.pre(new Button("目录"), node -> {
            node.setOnAction(e -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("选择补丁目录");
                File file = directoryChooser.showDialog(stage);
                if (file != null) {
                    refreshPatchTree(file);
                }
            });
        }));
        buildPatchTree();
        VBox.setVgrow(context.patchTree, Priority.ALWAYS);
        return new VBox(patchPackPathBox, context.patchTree);
    }
}
