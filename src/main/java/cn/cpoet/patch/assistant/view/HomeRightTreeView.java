package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.ParamNameConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.tree.*;
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

    private void buildPatchTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem markRootMenuItem = new MenuItem();
        markRootMenuItem.setOnAction(e -> {
            TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            TreeNode customRootNode = patchTreeInfo.getCustomRootNode();
            if (Objects.equals(customRootNode, selectedItem.getValue())) {
                patchTreeInfo.setCustomRootNode(null);
            } else {
                patchTreeInfo.setCustomRootNode(selectedItem.getValue());
            }
            refreshPatchTree(false, true);
        });
        MenuItem saveFileMenuItem = new MenuItem("保存文件");
        saveFileMenuItem.setOnAction(e -> saveFile(context.patchTree));
        MenuItem saveSourceFileMenuItem = new MenuItem("保存源文件");
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(context.patchTree));
        MenuItem viewPatchSign = new MenuItem("查看签名");
        viewPatchSign.setOnAction(e -> {
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            new PatchSignView(patchTreeInfo.getPatchSign()).showDialog(stage);
        });
        contextMenu.getItems().addAll(markRootMenuItem, saveFileMenuItem, saveSourceFileMenuItem, viewPatchSign);
        contextMenu.setOnShowing(e -> {
            TreeItem<TreeNode> selectedItem = context.patchTree.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            TreeNode selectedNode = selectedItem.getValue();
            if (selectedNode != patchTreeInfo.getRootNode() &&
                    selectedNode.getChildren() != null && !selectedNode.getChildren().isEmpty()) {
                markRootMenuItem.setVisible(true);
                if (Objects.equals(selectedNode, patchTreeInfo.getCustomRootNode())) {
                    markRootMenuItem.setText("取消根节点标记");
                } else {
                    markRootMenuItem.setText("标记为根节点");
                }
            } else {
                markRootMenuItem.setVisible(false);
            }
            if (selectedNode.isDir()) {
                saveFileMenuItem.setVisible(false);
                saveSourceFileMenuItem.setVisible(false);
            } else {
                saveFileMenuItem.setVisible(true);
                saveSourceFileMenuItem.setVisible(selectedNode.getText().endsWith(FileExtConst.DOT_CLASS));
            }
            viewPatchSign.setVisible(selectedItem.equals(context.getPatchTree().getRoot()));
        });
        context.patchTree.setContextMenu(contextMenu);
    }

    private void refreshPatchTree(File file) {
        PatchPackService patchPackService = PatchPackService.getInstance();
        PatchTreeInfo patchTreeInfo = patchPackService.getTreeNode(file);
        patchTree.setTreeInfo(patchTreeInfo);
        refreshPatchTree(true, true);
    }

    private void refreshPatchTree(boolean isBuildTreeItem, boolean isEmitEvent) {
        if (isEmitEvent) {
            context.patchTree.fireEvent(new Event(PatchTreeView.PATCH_TREE_REFRESHING));
        }
        TreeItem<TreeNode> rootItem = context.patchTree.getRoot();
        if (isBuildTreeItem) {
            if (rootItem == null) {
                rootItem = new CheckBoxTreeItem<>();
                context.patchTree.setRoot(rootItem);
            } else {
                rootItem.getChildren().clear();
            }
            TreeNodeUtil.buildNode(rootItem, patchTree.getTreeInfo().getRootNode());
        }
        TreeItem<TreeNode> curRootItem = patchTree.getTreeInfo().getCurRootNode().getTreeItem();
        TreeNodeUtil.expendedMappedOrCurRoot(context.totalInfo, rootItem, curRootItem);
        if (isEmitEvent) {
            context.patchTree.fireEvent(new Event(PatchTreeView.PATCH_TREE_REFRESH));
        }
    }

    private void refreshPatchMappedNode(boolean isRefreshReadme) {
        PatchPackService patchPackService = PatchPackService.getInstance();
        AppTreeInfo appTreeInfo = appTree.getTreeInfo();
        PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
        if (isRefreshReadme) {
            patchPackService.refreshReadmeNode(patchTreeInfo);
        }
        patchPackService.refreshMappedNode(context.totalInfo, appTreeInfo, patchTreeInfo);
    }

    private void initPatchTreeDrag() {
        context.patchTree.setOnDragOver(e -> {
            if (isDragFromTree(e)) {
                return;
            }
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

    private void buildPatchTree() {
        context.patchTree.setCellFactory(v -> new FileCheckBoxTreeCell(context));
        buildPatchTreeContextMenu();
        context.appTree.addEventHandler(AppTreeView.APP_TREE_REFRESHING, e -> refreshPatchMappedNode(false));
        context.appTree.addEventHandler(AppTreeView.APP_TREE_REFRESH, e -> refreshPatchTree(false, false));
        context.patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESHING, e -> refreshPatchMappedNode(true));
        context.patchTree.getSelectionModel().selectedItemProperty()
                .addListener((observableValue, oldVal, newVal) -> selectedLink(context.patchTree, context.appTree));
        context.patchTree.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TreeItem<TreeNode> selectedItem = context.patchTree.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    TreeNode selectedTreeNode = selectedItem.getValue();
                    if (selectedTreeNode.getText().endsWith(FileExtConst.DOT_ZIP)) {
                        if (PatchPackService.getInstance().buildNodeChildrenWithZip(selectedTreeNode, true)) {
                            TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode);
                        }
                        return;
                    }
                    new ContentView(selectedTreeNode).showDialog(stage);
                }
            }
        });
        initPatchTreeDrag();
        File file = getInitPatchFile();
        if (file != null) {
            refreshPatchTree(file);
        }
    }

    private File getInitPatchFile() {
        File file = null;
        String startPatchPath = AppContext.getInstance().getArg(ParamNameConst.START_PATCH);
        if (!StringUtil.isBlank(startPatchPath)) {
            file = FileUtil.getExistsDirOrFile(startPatchPath);
        }
        if (file != null) {
            return file;
        }
        String lastPatchPackPath = Configuration.getInstance().getLastPatchPackPath();
        if (!StringUtil.isBlank(lastPatchPackPath)) {
            file = FileUtil.getExistsDirOrFile(lastPatchPackPath);
        }
        return file;
    }

    public Node build() {
        HBox patchPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        patchPackPathBox.getChildren().add(new Label("补丁包:"));
        patchPackPathBox.getChildren().add(FXUtil.pre(new TextField(), node -> {
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            if (patchTreeInfo != null && patchTreeInfo.getRootNode() != null) {
                node.setText(patchTreeInfo.getRootNode().getPath());
            }
            context.patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESH, e -> {
                PatchTreeInfo treeInfo = patchTree.getTreeInfo();
                if (treeInfo != null && treeInfo.getRootNode() != null) {
                    node.setText(treeInfo.getRootNode().getPath());
                }
            });
            node.textProperty().addListener((observableValue, oldVal, newVal) -> {
                Configuration.getInstance().setLastPatchPackPath(newVal);
            });
        }));
        patchPackPathBox.getChildren().add(FXUtil.pre(new Button("选择"), node -> {
            node.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
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
