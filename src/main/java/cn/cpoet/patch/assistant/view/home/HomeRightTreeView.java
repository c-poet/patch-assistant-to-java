package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.ParamNameConst;
import cn.cpoet.patch.assistant.control.tree.*;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.service.compress.FileCompressor;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.content.ContentView;
import cn.cpoet.patch.assistant.view.node_mapped.NodeMappedView;
import cn.cpoet.patch.assistant.view.patch_sign.PatchSignView;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author CPoet
 */
public class HomeRightTreeView extends HomeTreeView {

    /**
     * 记录下一个根节点index，用于循环根节点
     */
    private int nextRootIndex;

    public HomeRightTreeView(Stage stage, HomeContext context) {
        super(stage, context);
    }

    private void handleMarkRoot() {
        TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
        TreeNode selectedNode = selectedItem.getValue();
        PatchMarkRootEvent event = new PatchMarkRootEvent(PatchTreeView.PATCH_MARK_ROOT_CHANGE);
        event.setTreeNode(selectedNode);
        patchTree.fireEvent(event);
    }

    protected void handleCancelMapped(ActionEvent event) {
        TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
        TreeNodeUtil.deepCleanMappedNode(context.totalInfo, selectedItem.getValue());
        appTree.refresh();
        patchTree.refresh();
    }

    private void handleViewPatchSign(ActionEvent event) {
        TreeNode treeNode = patchTree.getSelectionModel().getSelectedItem().getValue();
        PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
        PatchRootInfo patchRootInfo = patchTreeInfo.getRootInfoByNode(treeNode);
        new PatchSignView(patchRootInfo.getPatchSign()).showDialog(stage);
    }

    private void handleViewNodeMapped(ActionEvent event) {
        TreeNode rootNode = patchTree.getSelectionModel().getSelectedItem().getValue();
        new NodeMappedView(appTree.getTreeInfo().getRootNode(), rootNode).showDialog(stage);
    }

    private void buildPatchTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem cancelMappedMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.cancel-binding"));
        cancelMappedMenuItem.setOnAction(this::handleCancelMapped);
        contextMenu.getItems().add(cancelMappedMenuItem);

        MenuItem markRootMenuItem = new MenuItem();
        markRootMenuItem.setOnAction(e -> handleMarkRoot());
        contextMenu.getItems().add(markRootMenuItem);

        MenuItem saveFileMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.save-file"));
        saveFileMenuItem.setOnAction(e -> saveFile(patchTree));
        contextMenu.getItems().add(saveFileMenuItem);

        MenuItem saveSourceFileMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.save-source-file"));
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(patchTree));
        contextMenu.getItems().add(saveSourceFileMenuItem);

        MenuItem viewPatchSign = new MenuItem(I18nUtil.t("app.view.right-tree.view-sign"));
        viewPatchSign.setOnAction(this::handleViewPatchSign);
        contextMenu.getItems().add(viewPatchSign);

        MenuItem viewNodeMappedItem = new MenuItem(I18nUtil.t("app.view.right-tree.view-node-mapped"));
        viewNodeMappedItem.setOnAction(this::handleViewNodeMapped);
        contextMenu.getItems().add(viewNodeMappedItem);

        MenuItem focusPatchTreeItem = new MenuItem();
        focusPatchTreeItem.setOnAction(e -> context.focusTreeStatus.set(context.focusTreeStatus.get() != 0 ? 0 : 2));
        contextMenu.getItems().add(focusPatchTreeItem);

        contextMenu.setOnShowing(e -> {
            hideMenItem(contextMenu, item -> item == focusPatchTreeItem);
            focusPatchTreeItem.setText(I18nUtil.t((context.focusTreeStatus.get() & 2) == 2 ?
                    "app.view.right-tree.cancel-focus-patch-tree" : "app.view.right-tree.focus-patch-tree"));
            TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            TreeNode selectedNode = selectedItem.getValue();
            cancelMappedMenuItem.setVisible(selectedNode.getMappedNode() != null);
            if (!TreeNodeType.ROOT.equals(selectedNode.getType())
                    && CollectionUtil.isNotEmpty(selectedNode.getChildren())
                    && (selectedNode.isDir() || TreeNodeUtil.isCompressNode(selectedNode))
                    && TreeNodeUtil.isNotUnderCustomRoot(selectedNode)) {
                markRootMenuItem.setVisible(true);
                if (TreeNodeType.CUSTOM_ROOT.equals(selectedNode.getType())) {
                    markRootMenuItem.setText(I18nUtil.t("app.view.right-tree.cancel-root-mark"));
                } else {
                    markRootMenuItem.setText(I18nUtil.t("app.view.right-tree.mark-root"));
                }
            }
            if (!selectedNode.isDir()) {
                saveFileMenuItem.setVisible(true);
                saveSourceFileMenuItem.setVisible(selectedNode.getName().endsWith(FileExtConst.DOT_CLASS));
            }
            if (TreeNodeType.CUSTOM_ROOT.equals(selectedNode.getType())) {
                viewPatchSign.setVisible(true);
                viewNodeMappedItem.setVisible(appTree.getTreeInfo() != null);
            } else if (TreeNodeType.ROOT.equals(selectedNode.getType())) {
                viewPatchSign.setVisible(true);
                viewNodeMappedItem.setVisible(appTree.getTreeInfo() != null
                        && CollectionUtil.isEmpty(patchTree.getTreeInfo().getCustomRootInfoMap()));
            }
        });
        patchTree.setContextMenu(contextMenu);
    }

    private void refreshPatchTree(File file) {
        PatchTreeInfo patchTreeInfo = PatchPackService.INSTANCE.getTreeNode(file);
        patchTree.setTreeInfo(patchTreeInfo);
        refreshPatchTree(PatchTreeView.REFRESH_FLAG_EMIT_EVENT | PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM);
    }

    private void refreshPatchTree(int refreshFlag) {
        if ((refreshFlag & PatchTreeView.REFRESH_FLAG_EMIT_EVENT) == PatchTreeView.REFRESH_FLAG_EMIT_EVENT) {
            patchTree.fireEvent(new Event(PatchTreeView.PATCH_TREE_REFRESHING));
        }
        TreeItem<TreeNode> rootItem = patchTree.getRoot();
        if ((refreshFlag & PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM) == PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM) {
            if (rootItem == null) {
                rootItem = new CheckBoxTreeItem<>();
                patchTree.setRoot(rootItem);
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
            Map<TreeNode, PatchRootInfo> customRootInfoMap = patchTreeInfo.getCustomRootInfoMap();
            if (CollectionUtil.isNotEmpty(customRootInfoMap)) {
                treeItems = customRootInfoMap.keySet().stream().map(TreeNode::getTreeItem).collect(Collectors.toList());
            } else {
                treeItems = Collections.singletonList(patchTreeInfo.getRootNode().getTreeItem());
            }
            TreeNodeUtil.expendedMappedOrCurRoot(context.totalInfo, rootItem, treeItems);
        }
        if ((refreshFlag & PatchTreeView.REFRESH_FLAG_EMIT_EVENT) == PatchTreeView.REFRESH_FLAG_EMIT_EVENT) {
            patchTree.fireEvent(new Event(PatchTreeView.PATCH_TREE_REFRESH));
        }
    }

    private void refreshPatchMappedNode(boolean isRefreshReadme) {
        PatchTreeInfo treeInfo = patchTree.getTreeInfo();
        if (treeInfo == null) {
            return;
        }
        Map<TreeNode, PatchRootInfo> customRootInfoMap = treeInfo.getCustomRootInfoMap();
        if (CollectionUtil.isNotEmpty(customRootInfoMap)) {
            customRootInfoMap.forEach((node, info) -> refreshPatchMappedNode(isRefreshReadme, node));
        } else {
            refreshPatchMappedNode(isRefreshReadme, treeInfo.getRootNode());
        }
    }

    private void refreshPatchMappedNode(boolean isRefreshReadme, TreeNode rootNode) {
        AppTreeInfo appTreeInfo = appTree.getTreeInfo();
        PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
        if (isRefreshReadme) {
            PatchPackService.INSTANCE.refreshReadmeNode(patchTreeInfo, rootNode);
        }
        PatchPackService.INSTANCE.refreshMappedNode(context.totalInfo, appTreeInfo, patchTreeInfo, rootNode);
        patchTree.refresh();
        appTree.fireEvent(new Event(AppTreeView.APP_TREE_NONE_REFRESH_CALL));
    }

    private void cleanPatchMappedNode(TreeNode rootNode) {
        PatchPackService.INSTANCE.cleanMappedNode(context.totalInfo, rootNode, false);
        patchTree.refresh();
        appTree.refresh();
    }

    private void onDragOver(DragEvent event) {
        if (isDragFromTreeCell(event)) {
            return;
        }
        List<File> files = event.getDragboard().getFiles();
        if (files.size() == 1 && (files.get(0).isDirectory() || FileCompressor.isCompressFile(files.get(0).getName()))) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        refreshPatchTree(files.get(0));
    }

    private void initPatchTreeDrag() {
        patchTree.setOnDragOver(this::onDragOver);
        patchTree.setOnDragDropped(this::onDragDropped);
    }

    private void listenMarkRootChange(PatchMarkRootEvent event) {
        TreeNode treeNode = event.getTreeNode();
        if (TreeNodeType.CUSTOM_ROOT.equals(treeNode.getType())) {
            patchTree.getTreeInfo().removeCustomRootInfo(treeNode);
            cleanPatchMappedNode(treeNode);
            return;
        }
        treeNode.setType(TreeNodeType.CUSTOM_ROOT);
        PatchRootInfo patchRootInfo = PatchPackService.INSTANCE.createPatchRootInfo(treeNode);
        patchTree.getTreeInfo().addCustomRootInfo(treeNode, patchRootInfo);
        refreshPatchMappedNode(true, treeNode);
    }

    private void onMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                TreeNode selectedTreeNode = selectedItem.getValue();
                if (TreeNodeUtil.isCompressNode(selectedTreeNode)) {
                    if (PatchPackService.INSTANCE.buildChildrenWithCompress(selectedTreeNode, true)) {
                        TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode);
                    }
                } else {
                    new ContentView(selectedTreeNode).showDialog(stage);
                }
            }
        }
    }

    private void onKeyPressed(KeyEvent event) {
        // tab键用于切换根节点
        if (event.getCode() == KeyCode.TAB) {
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            Map<TreeNode, PatchRootInfo> customRootInfoMap = patchTreeInfo.getCustomRootInfoMap();
            if (CollectionUtil.isEmpty(customRootInfoMap)) {
                TreeItem<TreeNode> treeItem = patchTreeInfo.getRootNode().getTreeItem();
                patchTree.getSelectionModel().clearSelection();
                patchTree.getSelectionModel().select(treeItem);
                int row = patchTree.getRow(treeItem);
                patchTree.scrollTo(row);
            } else if (customRootInfoMap.size() == 1) {
                TreeItem<TreeNode> treeItem = CollectionUtil.getFirstKey(customRootInfoMap).getTreeItem();
                patchTree.getSelectionModel().clearSelection();
                patchTree.getSelectionModel().select(treeItem);
                int row = patchTree.getRow(treeItem);
                patchTree.scrollTo(row);
            } else {
                if (nextRootIndex >= customRootInfoMap.size()) {
                    nextRootIndex = 0;
                }
                int i = 0;
                TreeItem<TreeNode> treeItem = null;
                for (Map.Entry<TreeNode, PatchRootInfo> entry : customRootInfoMap.entrySet()) {
                    if (i == nextRootIndex) {
                        treeItem = entry.getKey().getTreeItem();
                        break;
                    }
                    ++i;
                }
                patchTree.getSelectionModel().clearSelection();
                patchTree.getSelectionModel().select(treeItem);
                int row = patchTree.getRow(treeItem);
                patchTree.scrollTo(row);
                ++nextRootIndex;
            }
            event.consume();
        }
    }

    private void buildPatchTree() {
        patchTree.setCellFactory(v -> new FileTreeCell(context));
        buildPatchTreeContextMenu();
        appTree.addEventHandler(AppTreeView.APP_TREE_REFRESHING, e -> refreshPatchMappedNode(false));
        appTree.addEventHandler(AppTreeView.APP_TREE_REFRESH, e -> refreshPatchTree(PatchTreeView.REFRESH_FLAG_NONE));
        patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESHING, e -> refreshPatchMappedNode(true));
        patchTree.addEventHandler(PatchTreeView.PATCH_MARK_ROOT_CHANGE, this::listenMarkRootChange);
        patchTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal)
                -> selectedLink(patchTree, appTree));
        patchTree.setOnMouseClicked(this::onMouseClicked);
        patchTree.setOnKeyPressed(this::onKeyPressed);
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
            patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESH, e -> {
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
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18nUtil.t("app.view.right-tree.patch-pack")
                            , "*" + FileExtConst.DOT_ZIP, "*" + FileExtConst.DOT_RAR));
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
        VBox.setVgrow(patchTree, Priority.ALWAYS);
        return new VBox(patchPackPathBox, patchTree);
    }
}
