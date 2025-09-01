package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.FocusTreeStatusConst;
import cn.cpoet.patch.assistant.constant.ParamNameConst;
import cn.cpoet.patch.assistant.control.tree.*;
import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.control.tree.node.FileNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.service.compress.FileDecompressor;
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
import javafx.scene.layout.StackPane;
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
        super(stage, context, new FastSearchControl(context.patchTree));
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
        new NodeMappedView(appTree.getTreeInfo().getRootNode(), rootNode, patchTree).showDialog(stage);
    }

    private void reloadPatchTree(TreeNode rootNode) {
        File file = ((FileNode) rootNode).getFile();
        if (file.exists()) {
            refreshPatchTree(file);
        }
    }

    private void handleReloadOrRefresh() {
        if (patchTree.getSelectionModel().isEmpty()) {
            reloadPatchTree(patchTree.getTreeInfo().getRootNode());
            return;
        }
        TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
        TreeNode rootNode = TreeNodeUtil.getUnderRootNode(selectedItem.getValue());
        if (TreeNodeType.ROOT.equals(rootNode.getType())) {
            reloadPatchTree(rootNode);
            return;
        }
        refreshPatchMappedNode(false, rootNode);
    }

    private void handleSelectPatchPack() {
        if (loadingFlag.get()) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18nUtil.t("app.view.right-tree.select-patch"));
        String lastPatchPackPath = Configuration.getInstance().getLastPatchPackPath();
        if (!StringUtil.isBlank(lastPatchPackPath)) {
            File dir = FileUtil.getExistsDirOrFile(FileNameUtil.getDirPath(lastPatchPackPath));
            fileChooser.setInitialDirectory(dir);
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18nUtil.t("app.view.right-tree.patch-pack")
                , "*" + FileExtConst.DOT_ZIP, "*" + FileExtConst.DOT_RAR));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            refreshPatchTree(file);
        }
    }

    private void buildPatchTreeContextMenu() {
        ContextMenu contextMenu = createContextMenu();

        MenuItem reloadOrRefreshMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.reload"));
        reloadOrRefreshMenuItem.setOnAction(e -> handleReloadOrRefresh());
        contextMenu.getItems().add(reloadOrRefreshMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = patchTree.getSingleSelectedNode();
            if (node == null) {
                reloadOrRefreshMenuItem.setVisible(false);
                return;
            }
            if (TreeNodeType.ROOT.equals(node.getType())) {
                reloadOrRefreshMenuItem.setText(I18nUtil.t("app.view.right-tree.reload"));
                reloadOrRefreshMenuItem.setVisible(true);
            } else if (TreeNodeType.CUSTOM_ROOT.equals(node.getType())) {
                reloadOrRefreshMenuItem.setText(I18nUtil.t("app.view.right-tree.refresh"));
                reloadOrRefreshMenuItem.setVisible(true);
            } else {
                reloadOrRefreshMenuItem.setVisible(false);
            }
        });

        MenuItem cancelMappedMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.cancel-binding"));
        cancelMappedMenuItem.setOnAction(this::handleCancelMapped);
        contextMenu.getItems().add(cancelMappedMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = patchTree.getSingleSelectedNode();
            cancelMappedMenuItem.setVisible(node != null && node.getMappedNode() != null);
        });

        RadioMenuItem markRootMenuItem = new RadioMenuItem(I18nUtil.t("app.view.right-tree.mark-root"));
        markRootMenuItem.setOnAction(e -> handleMarkRoot());
        contextMenu.getItems().add(markRootMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = patchTree.getSingleSelectedNode();
            if (node != null
                    && !TreeNodeType.ROOT.equals(node.getType())
                    && CollectionUtil.isNotEmpty(node.getChildren())
                    && (node.isDir() || TreeNodeUtil.isCompressNode(node))
                    && TreeNodeUtil.isNotUnderCustomRoot(node)) {
                markRootMenuItem.setVisible(true);
                markRootMenuItem.setSelected(TreeNodeType.CUSTOM_ROOT.equals(node.getType()));
            } else {
                markRootMenuItem.setVisible(false);
            }
        });

        MenuItem saveFileMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.save-file"));
        saveFileMenuItem.setOnAction(e -> saveFile(patchTree));
        contextMenu.getItems().add(saveFileMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = patchTree.getSingleSelectedNode();
            saveFileMenuItem.setVisible(node != null && !node.isDir());
        });

        MenuItem saveSourceFileMenuItem = new MenuItem(I18nUtil.t("app.view.right-tree.save-source-file"));
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(patchTree));
        contextMenu.getItems().add(saveSourceFileMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = patchTree.getSingleSelectedNode();
            saveSourceFileMenuItem.setVisible(node != null && !node.isDir() && node.getName().endsWith(FileExtConst.DOT_CLASS));
        });

        MenuItem viewPatchSign = new MenuItem(I18nUtil.t("app.view.right-tree.view-sign"));
        viewPatchSign.setOnAction(this::handleViewPatchSign);
        contextMenu.getItems().add(viewPatchSign);
        addMenuItemPreFunc(() -> {
            TreeNode node = patchTree.getSingleSelectedNode();
            viewPatchSign.setVisible(node != null && (TreeNodeType.CUSTOM_ROOT.equals(node.getType()) || TreeNodeType.ROOT.equals(node.getType())));
        });

        MenuItem viewNodeMappedItem = new MenuItem(I18nUtil.t("app.view.right-tree.view-node-mapped"));
        viewNodeMappedItem.setOnAction(this::handleViewNodeMapped);
        contextMenu.getItems().add(viewNodeMappedItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = patchTree.getSingleSelectedNode();
            if (node == null) {
                viewNodeMappedItem.setVisible(false);
            } else if (TreeNodeType.CUSTOM_ROOT.equals(node.getType())) {
                viewNodeMappedItem.setVisible(appTree.getTreeInfo() != null);
            } else if (TreeNodeType.ROOT.equals(node.getType())) {
                viewNodeMappedItem.setVisible(appTree.getTreeInfo() != null
                        && CollectionUtil.isEmpty(patchTree.getTreeInfo().getCustomRootInfoMap()));
            } else {
                viewNodeMappedItem.setVisible(false);
            }
        });

        MenuItem openInExplorerItem = new MenuItem(I18nUtil.t("app.view.right-tree.open-in-explorer"));
        openInExplorerItem.setOnAction(e -> handleOpenInExplorer(e, patchTree));
        contextMenu.getItems().add(openInExplorerItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = patchTree.getSingleSelectedNode();
            openInExplorerItem.setVisible(node instanceof FileNode && !(node instanceof CompressNode));
        });

        RadioMenuItem focusPatchTreeItem = new RadioMenuItem(I18nUtil.t("app.view.right-tree.focus-patch-tree"));
        focusPatchTreeItem.setSelected((context.focusTreeStatus.get() & FocusTreeStatusConst.PATCH) == FocusTreeStatusConst.PATCH);
        focusPatchTreeItem.setOnAction(e -> context.focusTreeStatus.set(context.focusTreeStatus.get() != FocusTreeStatusConst.ALL ? FocusTreeStatusConst.ALL : FocusTreeStatusConst.PATCH));
        contextMenu.getItems().add(focusPatchTreeItem);

        patchTree.setContextMenu(contextMenu);
    }

    private void refreshPatchTree(File file) {
        loadingFlag.set(true);
        UIUtil.runNotUI(() -> {
            try {
                PatchTreeInfo oldTreeInfo = patchTree.getTreeInfo();
                if (oldTreeInfo != null && oldTreeInfo.getRootNode() != null) {
                    cleanPatchMappedNode(oldTreeInfo.getRootNode());
                }
                PatchTreeInfo patchTreeInfo = PatchPackService.INSTANCE.getTreeNode(file);
                patchTree.setTreeInfo(patchTreeInfo);
                refreshPatchTree(PatchTreeView.REFRESH_FLAG_EMIT_EVENT | PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM);
            } finally {
                loadingFlag.set(false);
            }
        });
    }

    private void refreshPatchTree(int refreshFlag) {
        UIUtil.runUI(() -> {
            if ((refreshFlag & PatchTreeView.REFRESH_FLAG_EMIT_EVENT) == PatchTreeView.REFRESH_FLAG_EMIT_EVENT) {
                patchTree.fireEvent(new Event(PatchTreeView.PATCH_TREE_REFRESHING));
            }
            TreeItem<TreeNode> rootItem = patchTree.getRoot();
            if ((refreshFlag & PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM) == PatchTreeView.REFRESH_FLAG_BUILD_TREE_ITEM) {
                if (rootItem == null) {
                    rootItem = new FileTreeItem();
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
        });
    }

    private void refreshPatchMappedNode(boolean isRefreshReadme) {
        UIUtil.runNotUI(() -> {
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
        });
    }

    private void refreshPatchMappedNode(boolean isRefreshReadme, TreeNode rootNode) {
        UIUtil.runNotUI(() -> {
            AppTreeInfo appTreeInfo = appTree.getTreeInfo();
            PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
            if (isRefreshReadme) {
                PatchPackService.INSTANCE.refreshReadmeNode(patchTreeInfo, rootNode);
            }
            PatchPackService.INSTANCE.refreshMappedNode(context.totalInfo, appTreeInfo, patchTreeInfo, rootNode);
            UIUtil.runUI(() -> {
                patchTree.refresh();
                appTree.fireEvent(new Event(AppTreeView.APP_TREE_NONE_REFRESH_CALL));
            });
        });
    }

    private void cleanPatchMappedNode(TreeNode rootNode) {
        UIUtil.runNotUI(() -> {
            PatchPackService.INSTANCE.cleanMappedNode(context.totalInfo, rootNode, false);
            UIUtil.runUI(() -> {
                patchTree.refresh();
                appTree.refresh();
            });
        });
    }

    private void onDragOver(DragEvent event) {
        if (isDragFromTreeCell(event) || isLoadingFlag()) {
            return;
        }
        List<File> files = event.getDragboard().getFiles();
        if (files.size() == 1 && (files.get(0).isDirectory() || FileDecompressor.isCompressFile(files.get(0).getName()))) {
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
        UIUtil.runNotUI(() -> {
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
        });
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

    private void onKeyReleased(KeyEvent event) {
        if (KeyCode.ENTER.equals(event.getCode())) {
            handleEnterKey(event, patchTree);
        } else if (KeyCode.F5.equals(event.getCode()) || (event.isControlDown() && KeyCode.R.equals(event.getCode()))) {
            handleReloadOrRefresh();
            event.consume();
        } else if (event.isControlDown() && KeyCode.O.equals(event.getCode())) {
            handleSelectPatchPack();
            event.consume();
        }
        fastSearchControl.onKeyReleased(event);
    }

    private Node buildPatchTree() {
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
        patchTree.setOnKeyReleased(this::onKeyReleased);
        initPatchTreeDrag();
        UIUtil.runNotUI(() -> {
            File file = getInitPatchFile();
            if (file != null) {
                refreshPatchTree(file);
            }
        });
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(patchTree);
        fastSearchControl.fillNode(stackPane);
        return stackPane;
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

    private void addPatchPathLabel(HBox patchPackPathBox) {
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
    }

    private void addSelectBtn(HBox patchPackPathBox) {
        patchPackPathBox.getChildren().add(FXUtil.pre(new Button(I18nUtil.t("app.view.right-tree.select")), node -> {
            node.setOnAction(e -> handleSelectPatchPack());
            loadingFlagProperty().addListener((observableValue, oldVal, newVal) -> {
                if (newVal) {
                    UIUtil.runUI(() -> {
                        node.setText(I18nUtil.t("app.view.right-tree.select-loading"));
                        node.setDisable(true);
                    });
                } else {
                    UIUtil.runUI(() -> {
                        node.setText(I18nUtil.t("app.view.right-tree.select"));
                        node.setDisable(false);
                    });
                }
            });
        }));
    }

    private void addSelectDirBtn(HBox patchPackPathBox) {
        patchPackPathBox.getChildren().add(FXUtil.pre(new Button(I18nUtil.t("app.view.right-tree.patch-directory")), node -> {
            node.setOnAction(e -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle(I18nUtil.t("app.view.right-tree.select-patch-directory"));
                String lastPatchPackPath = Configuration.getInstance().getLastPatchPackPath();
                if (!StringUtil.isBlank(lastPatchPackPath)) {
                    File file = FileUtil.getExistsDirOrFile(lastPatchPackPath);
                    directoryChooser.setInitialDirectory(file == null ? null : (file.isFile() ? file.getParentFile() : file));
                }
                File file = directoryChooser.showDialog(stage);
                if (file != null) {
                    refreshPatchTree(file);
                }
            });
            loadingFlagProperty().addListener((observableValue, oldVal, newVal) -> {
                if (newVal) {
                    UIUtil.runUI(() -> {
                        node.setText(I18nUtil.t("app.view.right-tree.select-loading"));
                        node.setDisable(true);
                    });
                } else {
                    UIUtil.runUI(() -> {
                        node.setText(I18nUtil.t("app.view.right-tree.patch-directory"));
                        node.setDisable(false);
                    });
                }
            });
        }));
    }

    public Node build() {
        HBox patchPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        addPatchPathLabel(patchPackPathBox);
        addSelectBtn(patchPackPathBox);
        addSelectDirBtn(patchPackPathBox);
        Node patchTreePane = buildPatchTree();
        VBox.setVgrow(patchTreePane, Priority.ALWAYS);
        return new VBox(patchPackPathBox, patchTreePane);
    }
}
