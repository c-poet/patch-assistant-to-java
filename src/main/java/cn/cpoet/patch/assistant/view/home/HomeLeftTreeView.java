package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.FocusTreeStatusConst;
import cn.cpoet.patch.assistant.control.tree.*;
import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.control.tree.node.FileNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.control.tree.node.VirtualNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.AppPackService;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.content.ContentView;
import javafx.collections.ObservableList;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author CPoet
 */
public class HomeLeftTreeView extends HomeTreeView {

    public HomeLeftTreeView(Stage stage, HomeContext context) {
        super(stage, context, new FastSearchControl(context.appTree));
    }

    private void handleManualDel(ActionEvent event) {
        ButtonType buttonType = AlterUtil.confirm(stage, I18nUtil.t("app.view.left-tree.delete-confirm"), ButtonType.YES, ButtonType.NO);
        if (ButtonType.YES.equals(buttonType)) {
            List<TreeNode> treeNodes = appTree.getSelectionModel().getSelectedItems().stream()
                    .map(TreeItem::getValue)
                    .collect(Collectors.toList());
            treeNodes.forEach(node -> {
                TreeNodeUtil.deepCleanMappedNode(context.totalInfo, node);
                TreeNodeUtil.removeNodeChild(node);
                TreeNodeUtil.countNodeType(context.totalInfo, node, TreeNodeType.MANUAL_DEL);
            });
            appTree.getSelectionModel().clearSelection();
            patchTree.refresh();
        }
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
        appTree.tryEdit(selectedItem);
    }

    private void handleMkdir(ActionEvent event) {
        TreeItem<TreeNode> treeItem = appTree.getSelectionModel().getSelectedItem();
        TreeNode treeNode = treeItem.getValue();
        VirtualNode newNode = new VirtualNode();
        String name = getNextMkdirName(treeNode);
        newNode.setName(name);
        newNode.setModifyTime(LocalDateTime.now());
        newNode.setPath(FileNameUtil.joinPath(treeNode.getPath(), name));
        newNode.setDir(true);
        newNode.setParent(treeNode);
        List<TreeNode> children = treeNode.getAndInitChildren();
        children.add(newNode);
        TreeItem<TreeNode> newTreeItem = new FileTreeItem();
        TreeNodeUtil.bindTreeNodeAndItem(newNode, newTreeItem);
        treeItem.getChildren().add(children.indexOf(newNode), newTreeItem);
        appTree.getSelectionModel().clearSelection();
        appTree.getSelectionModel().select(newTreeItem);
        appTree.tryEdit(newTreeItem);
    }

    private String getNextMkdirName(TreeNode node) {
        String name = I18nUtil.t("app.view.left-tree.mkdir-new");
        if (CollectionUtil.isEmpty(node.getChildren())) {
            return name;
        }
        Set<String> nameSet = node.getChildren().stream().map(TreeNode::getName).collect(Collectors.toSet());
        if (!nameSet.contains(name)) {
            return name;
        }
        int i = 1;
        String newName;
        do {
            newName = name + " " + i++;
        } while (nameSet.contains(newName));
        return newName;
    }

    private void handleReload(ActionEvent event) {
        FileNode rootNode = (FileNode) appTree.getTreeInfo().getRootNode();
        if (rootNode.getFile().exists()) {
            refreshAppTree(rootNode.getFile());
        }
    }

    private void handleSelectAppPack() {
        if (loadingFlag.get()) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18nUtil.t("app.view.left-tree.select-jar"));
        String lastAppPackPath = Configuration.getInstance().getLastAppPackPath();
        if (!StringUtil.isBlank(lastAppPackPath)) {
            File dir = FileUtil.getExistsDirOrFile(FileNameUtil.getDirPath(lastAppPackPath));
            fileChooser.setInitialDirectory(dir);
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18nUtil.t("app.view.left-tree.java-package"), "*.jar"));
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        refreshAppTree(file);
    }

    private void buildAppTreeContextMenu() {
        ContextMenu contextMenu = createContextMenu();
        MenuItem mkdirMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.mkdir"));
        mkdirMenuItem.setOnAction(this::handleMkdir);
        contextMenu.getItems().add(mkdirMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = appTree.getSingleSelectedNode();
            mkdirMenuItem.setVisible(node != null && node.isDir());
        });

        MenuItem renameMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.rename"));
        renameMenuItem.setOnAction(this::handleRename);
        contextMenu.getItems().add(renameMenuItem);
        addMenuItemPreFunc(() -> renameMenuItem.setVisible(appTree.isSingleSelectedNode()));

        MenuItem reloadMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.reload"));
        reloadMenuItem.setOnAction(this::handleReload);
        contextMenu.getItems().add(reloadMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = appTree.getSingleSelectedNode();
            reloadMenuItem.setVisible(node != null && TreeNodeType.ROOT.equals(node.getType()));
        });

        MenuItem manualDelMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.delete"));
        manualDelMenuItem.setOnAction(this::handleManualDel);
        contextMenu.getItems().add(manualDelMenuItem);
        addMenuItemPreFunc(() -> {
            ObservableList<TreeItem<TreeNode>> selectedItems = appTree.getSelectionModel().getSelectedItems();
            boolean isNoneNode = CollectionUtil.isEmpty(selectedItems) || selectedItems.stream().anyMatch(item -> item.equals(appTree.getRoot()) || !item.getValue().getType().equals(TreeNodeType.NONE));
            manualDelMenuItem.setVisible(!isNoneNode);
        });

        RadioMenuItem markDelMenuItem = new RadioMenuItem(I18nUtil.t("app.view.left-tree.mark-delete"));
        markDelMenuItem.setOnAction(this::handleMarkDel);
        contextMenu.getItems().add(markDelMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = appTree.getSingleSelectedNode();
            if (node != null) {
                markDelMenuItem.setVisible(TreeNodeType.DEL.equals(node.getType()) || TreeNodeType.NONE.equals(node.getType()));
                markDelMenuItem.setSelected(TreeNodeType.DEL.equals(node.getType()));
            } else {
                markDelMenuItem.setVisible(false);
            }
        });

        MenuItem saveFileMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.save-file"));
        saveFileMenuItem.setOnAction(e -> saveFile(appTree));
        contextMenu.getItems().add(saveFileMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = appTree.getSingleSelectedNode();
            saveFileMenuItem.setVisible(node != null && !node.isDir());
        });

        MenuItem saveSourceFileMenuItem = new MenuItem(I18nUtil.t("app.view.left-tree.save-source-file"));
        saveSourceFileMenuItem.setOnAction(e -> saveSourceFile(appTree));
        contextMenu.getItems().add(saveSourceFileMenuItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = appTree.getSingleSelectedNode();
            saveSourceFileMenuItem.setVisible(node != null && !node.isDir() && node.getName().endsWith(FileExtConst.DOT_CLASS));
        });

        MenuItem openInExplorerItem = new MenuItem(I18nUtil.t("app.view.left-tree.open-in-explorer"));
        openInExplorerItem.setOnAction(e -> handleOpenInExplorer(e, appTree));
        contextMenu.getItems().add(openInExplorerItem);
        addMenuItemPreFunc(() -> {
            TreeNode node = appTree.getSingleSelectedNode();
            openInExplorerItem.setVisible(node instanceof FileNode && !(node instanceof CompressNode));
        });

        RadioMenuItem focusAppTreeItem = new RadioMenuItem(I18nUtil.t("app.view.left-tree.focus-app-tree"));
        focusAppTreeItem.setSelected((context.focusTreeStatus.get() & FocusTreeStatusConst.APP) == FocusTreeStatusConst.APP);
        focusAppTreeItem.setOnAction(e -> context.focusTreeStatus.set(context.focusTreeStatus.get() != FocusTreeStatusConst.ALL ? FocusTreeStatusConst.ALL : FocusTreeStatusConst.APP));
        contextMenu.getItems().add(focusAppTreeItem);

        appTree.setContextMenu(contextMenu);
    }

    private void refreshAppTree(File file) {
        loadingFlag.set(true);
        UIUtil.runNotUI(() -> {
            try {
                AppTreeInfo appTreeInfo = AppPackService.INSTANCE.getTreeNode(file);
                appTree.setTreeInfo(appTreeInfo);
                refreshAppTree(AppTreeView.REFRESH_FLAG_EMIT_EVENT);
            } finally {
                loadingFlag.set(false);
            }
        });
    }

    private void refreshAppTree(int refreshFlag) {
        UIUtil.runUI(() -> {
            if ((refreshFlag & AppTreeView.REFRESH_FLAG_EMIT_EVENT) == AppTreeView.REFRESH_FLAG_EMIT_EVENT) {
                appTree.fireEvent(new Event(AppTreeView.APP_TREE_REFRESHING));
            }
            TreeItem<TreeNode> rootItem = appTree.getRoot();
            if (rootItem == null) {
                rootItem = new FileTreeItem();
                appTree.setRoot(rootItem);
            } else {
                rootItem.getChildren().clear();
            }
            AppTreeInfo treeInfo = appTree.getTreeInfo();
            if (treeInfo != null) {
                TreeNodeUtil.buildNode(appTree.getRoot(), treeInfo.getRootNode(), OnlyChangeFilter.INSTANCE);
            }
            if ((refreshFlag & AppTreeView.REFRESH_FLAG_EMIT_EVENT) == AppTreeView.REFRESH_FLAG_EMIT_EVENT) {
                appTree.fireEvent(new Event(AppTreeView.APP_TREE_REFRESH));
            }
        });
    }

    private void onDragOver(DragEvent event) {
        if (isDragFromTreeCell(event) || isLoadingFlag()) {
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
            if (selectedItem != null && selectedItem.getValue() != null) {
                TreeNode selectedTreeNode = selectedItem.getValue();
                if (selectedTreeNode.getName().endsWith(FileExtConst.DOT_JAR)) {
                    if (AppPackService.INSTANCE.buildChildrenWithCompress(selectedTreeNode, false)) {
                        TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode, OnlyChangeFilter.INSTANCE);
                    }
                } else {
                    new ContentView(selectedTreeNode).showDialog(stage);
                }
            }
            event.consume();
        }
    }

    private void onKeyReleased(KeyEvent event) {
        if (KeyCode.DELETE.equals(event.getCode())) {
            if (!appTree.getSelectionModel().isEmpty()) {
                handleManualDel(null);
            }
            event.consume();
        } else if (KeyCode.F2.equals(event.getCode())) {
            if (!appTree.getSelectionModel().isEmpty()) {
                handleRename(null);
            }
            event.consume();
        } else if (KeyCode.ENTER.equals(event.getCode())) {
            handleEnterKey(event, appTree);
        } else if (KeyCode.F5.equals(event.getCode()) || (event.isControlDown() && KeyCode.R.equals(event.getCode()))) {
            handleReload(null);
            event.consume();
        } else if (event.isControlDown() && KeyCode.O.equals(event.getCode())) {
            handleSelectAppPack();
            event.consume();
        }
        fastSearchControl.onKeyReleased(event);
    }

    private Node buildAppTree() {
        appTree.setCellFactory(treeView -> new EditFileTreeCell(context));
        buildAppTreeContextMenu();
        patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESH, e -> refreshAppTree(AppTreeView.REFRESH_FLAG_NONE));
        appTree.addEventHandler(AppTreeView.APP_TREE_NONE_REFRESH_CALL, e -> refreshAppTree(AppTreeView.REFRESH_FLAG_NONE));
        appTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal)
                -> selectedLink(appTree, patchTree));
        appTree.setOnMouseClicked(this::onMouseClicked);
        appTree.setOnKeyReleased(this::onKeyReleased);
        initAppTreeDrag();
        UIUtil.runNotUI(() -> {
            String lastAppPackPath = Configuration.getInstance().getLastAppPackPath();
            if (StringUtil.isBlank(lastAppPackPath)) {
                return;
            }
            File file = FileUtil.getExistsFile(lastAppPackPath);
            if (file != null) {
                refreshAppTree(file);
            }
        });
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(appTree);
        fastSearchControl.fillNode(stackPane);
        return stackPane;
    }

    private void addPackPathLabel(HBox appPackPathBox) {
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
    }

    private void addSelectBtn(HBox appPackPathBox) {
        appPackPathBox.getChildren().add(FXUtil.pre(new Button(I18nUtil.t("app.view.left-tree.select")), node -> {
            node.setOnAction(e -> handleSelectAppPack());
            loadingFlagProperty().addListener((observableValue, oldVal, newVal) -> {
                if (newVal) {
                    UIUtil.runUI(() -> {
                        node.setText(I18nUtil.t("app.view.left-tree.select-loading"));
                        node.setDisable(true);
                    });
                } else {
                    UIUtil.runUI(() -> {
                        node.setText(I18nUtil.t("app.view.left-tree.select"));
                        node.setDisable(false);
                    });
                }
            });
        }));
    }

    public Node build() {
        HBox appPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        addPackPathLabel(appPackPathBox);
        addSelectBtn(appPackPathBox);
        Node appTreePane = buildAppTree();
        VBox.setVgrow(appTreePane, Priority.ALWAYS);
        return new VBox(appPackPathBox, appTreePane);
    }
}
