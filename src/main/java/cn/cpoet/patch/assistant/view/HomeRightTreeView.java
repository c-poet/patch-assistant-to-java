package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.ParamNameConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.tree.*;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CPoet
 */
public class HomeRightTreeView extends HomeTreeView {

    public HomeRightTreeView(Stage stage, HomeContext context) {
        super(stage, context);
    }

    private void handleMarkRoot() {
        TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
        TreeNode selectedNode = selectedItem.getValue();
        PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
        List<PatchSignTreeNode> markRootNodes = patchTreeInfo.getAndInitMarkRootNodes();
        PatchPackService patchPackService = PatchPackService.getInstance();
        PatchMarkRootEvent event = new PatchMarkRootEvent(PatchTreeView.PATCH_MARK_ROOT_CHANGE);
        if (selectedNode instanceof PatchSignTreeNode) {
            markRootNodes.remove((PatchSignTreeNode) selectedNode);
            patchPackService.unwrapPatchSign((PatchSignTreeNode) selectedNode);
            event.setAdd(false);
            event.setTreeNode((PatchSignTreeNode) selectedNode);
        } else {
            PatchSignTreeNode patchSignTreeNode = patchPackService.wrapPatchSign(selectedNode);
            markRootNodes.add(patchSignTreeNode);
            event.setAdd(true);
            event.setTreeNode(patchSignTreeNode);
        }
        patchTree.fireEvent(event);
    }

    private void buildPatchTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem markRootMenuItem = new MenuItem();
        markRootMenuItem.setOnAction(e -> handleMarkRoot());
        MenuItem cancelMappedMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.cancel-binding"));
        cancelMappedMenuItem.setOnAction(e -> cancelMapped(patchTree));
        MenuItem saveFileMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.save-file"));
        saveFileMenuItem.setOnAction(e -> saveFile(context.patchTree));
        MenuItem saveSourceFileMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.save-source-file"));
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(context.patchTree));
        MenuItem viewPatchSign = new MenuItem(I18nUtil.t("app.view.right-tree.view-sign"));
        viewPatchSign.setOnAction(e -> {
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            new PatchSignView(patchTreeInfo.getRootNode().getPatchSign()).showDialog(stage);
        });
        contextMenu.getItems().addAll(cancelMappedMenuItem, markRootMenuItem, saveFileMenuItem, saveSourceFileMenuItem, viewPatchSign);
        contextMenu.setOnShowing(e -> {
            TreeItem<TreeNode> selectedItem = context.patchTree.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            TreeNode selectedNode = selectedItem.getValue();
            cancelMappedMenuItem.setVisible(selectedNode.getMappedNode() != null);
            if (selectedNode != patchTreeInfo.getRootNode() && CollectionUtil.isNotEmpty(selectedNode.getChildren()) &&
                    (selectedNode.isDir() || selectedNode.getName().endsWith(FileExtConst.DOT_ZIP))) {
                markRootMenuItem.setVisible(true);
                if (selectedNode instanceof PatchSignTreeNode) {
                    markRootMenuItem.setText(I18nUtil.t("app.view.right-tree.cancel-root-mark"));
                } else {
                    markRootMenuItem.setText(I18nUtil.t("app.view.right-tree.mark-root"));
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
        refreshPatchTree(PatchTreeView.REFRESH_FLAG_EMIT_EVENT | PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM);
    }

    private void refreshPatchTree(int refreshFlag) {
        if ((refreshFlag & PatchTreeView.REFRESH_FLAG_EMIT_EVENT) == PatchTreeView.REFRESH_FLAG_EMIT_EVENT) {
            context.patchTree.fireEvent(new Event(PatchTreeView.PATCH_TREE_REFRESHING));
        }
        TreeItem<TreeNode> rootItem = context.patchTree.getRoot();
        if ((refreshFlag & PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM) == PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM) {
            if (rootItem == null) {
                rootItem = new CheckBoxTreeItem<>();
                context.patchTree.setRoot(rootItem);
            } else {
                rootItem.getChildren().clear();
            }
            if (patchTree.getTreeInfo() != null) {
                TreeNodeUtil.buildNode(rootItem, patchTree.getTreeInfo().getRootNode());
            }
        }
        if (patchTree.getTreeInfo() != null) {
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            List<TreeItem<TreeNode>> treeItems;
            List<PatchSignTreeNode> markRootNodes = patchTreeInfo.getMarkRootNodes();
            if (CollectionUtil.isNotEmpty(markRootNodes)) {
                treeItems = markRootNodes.stream().map(TreeNode::getTreeItem).collect(Collectors.toList());
            } else {
                treeItems = Collections.singletonList(patchTreeInfo.getRootNode().getTreeItem());
            }
            TreeNodeUtil.expendedMappedOrCurRoot(context.totalInfo, rootItem, treeItems);
        }
        if ((refreshFlag & PatchTreeView.REFRESH_FLAG_EMIT_EVENT) == PatchTreeView.REFRESH_FLAG_EMIT_EVENT) {
            context.patchTree.fireEvent(new Event(PatchTreeView.PATCH_TREE_REFRESH));
        }
    }

    private void refreshPatchMappedNode(boolean isRefreshReadme) {
        PatchTreeInfo treeInfo = patchTree.getTreeInfo();
        if (treeInfo == null) {
            return;
        }
        List<PatchSignTreeNode> markRootNodes = treeInfo.getMarkRootNodes();
        if (CollectionUtil.isNotEmpty(markRootNodes)) {
            markRootNodes.forEach(markRootNode -> refreshPatchMappedNode(isRefreshReadme, markRootNode));
        } else {
            refreshPatchMappedNode(isRefreshReadme, treeInfo.getRootNode());
        }
    }

    private void refreshPatchMappedNode(boolean isRefreshReadme, PatchSignTreeNode rootNode) {
        PatchPackService patchPackService = PatchPackService.getInstance();
        AppTreeInfo appTreeInfo = appTree.getTreeInfo();
        PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
        if (isRefreshReadme) {
            patchPackService.refreshReadmeNode(rootNode);
        }
        patchPackService.refreshMappedNode(context.totalInfo, appTreeInfo, patchTreeInfo, rootNode);
        appTree.fireEvent(new Event(AppTreeView.APP_TREE_NONE_REFRESH_CALL));
    }

    private void cleanPatchMappedNode(PatchSignTreeNode rootNode) {
        PatchPackService patchPackService = PatchPackService.getInstance();
        patchPackService.cleanMappedNode(context.totalInfo, rootNode, false);
        appTree.refresh();
    }

    private void onDragOver(DragEvent event) {
        if (isDragFromTreeCell(event)) {
            return;
        }
        List<File> files = event.getDragboard().getFiles();
        if (files.size() == 1 && (files.get(0).isDirectory() ||
                files.get(0).getName().endsWith(FileExtConst.DOT_ZIP))) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        refreshPatchTree(files.get(0));
    }

    private void initPatchTreeDrag() {
        context.patchTree.setOnDragOver(this::onDragOver);
        context.patchTree.setOnDragDropped(this::onDragDropped);
    }

    private void listenMarkRootChange(PatchMarkRootEvent event) {
        PatchSignTreeNode treeNode = event.getTreeNode();
        if (event.isAdd()) {
            refreshPatchMappedNode(true, treeNode);
            return;
        }
        cleanPatchMappedNode(treeNode);
    }

    private void onMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            TreeItem<TreeNode> selectedItem = context.patchTree.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                TreeNode selectedTreeNode = selectedItem.getValue();
                if (selectedTreeNode.getText().endsWith(FileExtConst.DOT_ZIP)) {
                    if (PatchPackService.getInstance().buildNodeChildrenWithZip(selectedTreeNode, true)) {
                        TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode);
                    }
                } else {
                    new ContentView(selectedTreeNode).showDialog(stage);
                }
            }
        }
    }

    private void buildPatchTree() {
        context.patchTree.setCellFactory(v -> new FileTreeCell(context));
        buildPatchTreeContextMenu();
        context.appTree.addEventHandler(AppTreeView.APP_TREE_REFRESHING, e -> refreshPatchMappedNode(false));
        context.appTree.addEventHandler(AppTreeView.APP_TREE_REFRESH, e -> refreshPatchTree(PatchTreeView.REFRESH_FLAG_NONE));
        context.patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESHING, e -> refreshPatchMappedNode(true));
        context.patchTree.addEventHandler(PatchTreeView.PATCH_MARK_ROOT_CHANGE, this::listenMarkRootChange);
        context.patchTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal)
                -> selectedLink(context.patchTree, context.appTree));
        context.patchTree.setOnMouseClicked(this::onMouseClicked);
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
        patchPackPathBox.getChildren().add(new Label(I18nUtil.t("app.view.right-tree.patch-package")));
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
            node.textProperty().addListener((observableValue, oldVal, newVal) -> Configuration.getInstance().setLastPatchPackPath(newVal));
        }));
        patchPackPathBox.getChildren().add(FXUtil.pre(new Button(I18nUtil.t("app.view.right-tree.select")), node ->
                node.setOnAction(e -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(I18nUtil.t("app.view.right-tree.select-patch"));
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18nUtil.t("app.view.right-tree.patch-pack"), "*.zip"));
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        refreshPatchTree(file);
                    }
                })
        ));
        patchPackPathBox.getChildren().add(FXUtil.pre(new Button(I18nUtil.t("app.view.right-tree.patch-directory")), node ->
                node.setOnAction(e -> {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle(I18nUtil.t("app.view.right-tree.select-patch-directory"));
                    File file = directoryChooser.showDialog(stage);
                    if (file != null) {
                        refreshPatchTree(file);
                    }
                })
        ));
        buildPatchTree();
        VBox.setVgrow(context.patchTree, Priority.ALWAYS);
        return new VBox(patchPackPathBox, context.patchTree);
    }
}
