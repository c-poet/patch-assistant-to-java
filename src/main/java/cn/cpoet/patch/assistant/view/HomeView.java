package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.AppPackService;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.tree.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class HomeView extends HomeContext {

    private final Stage stage;

    public HomeView(Stage stage) {
        this.stage = stage;
    }

    protected Node buildHeader() {

        Configuration configuration = Configuration.getInstance();

        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        CheckBox checkSelectedLink = new CheckBox("选中联动");
        checkSelectedLink.setSelected(Boolean.TRUE.equals(configuration.getIsSelectedLinked()));
        checkSelectedLink.setOnAction(e -> configuration.setIsSelectedLinked(!Boolean.TRUE.equals(configuration.getIsSelectedLinked())));
        headerBox.getChildren().add(checkSelectedLink);

        CheckBox showFileDetail = new CheckBox("文件详情");
        showFileDetail.setSelected(Boolean.TRUE.equals(configuration.getIsShowFileDetail()));
        showFileDetail.setOnAction(e -> {
            configuration.setIsShowFileDetail(!Boolean.TRUE.equals(configuration.getIsShowFileDetail()));
            appTree.refresh();
            patchTree.refresh();
        });
        headerBox.getChildren().add(showFileDetail);

        CheckBox onlyChanges = new CheckBox("仅看变动");
        onlyChanges.setSelected(Boolean.TRUE.equals(configuration.getIsOnlyChanges()));
        onlyChanges.setOnAction(e -> {
            configuration.setIsOnlyChanges(!Boolean.TRUE.equals(configuration.getIsOnlyChanges()));
            appTree.getRoot().getChildren().clear();
            TreeNodeUtil.buildNode(appTree.getRoot(), appTree.getRoot().getValue(), OnlyChangeFilter.INSTANCE);
            appTree.refresh();
        });
        headerBox.getChildren().add(onlyChanges);

        CheckBox checkDockerImage = new CheckBox("Docker镜像");
        checkDockerImage.setSelected(Boolean.TRUE.equals(configuration.getIsDockerImage()));
        checkDockerImage.setOnAction(e -> configuration.setIsDockerImage(!Boolean.TRUE.equals(configuration.getIsDockerImage())));
        headerBox.getChildren().add(checkDockerImage);

        headerBox.getChildren().add(FXUtil.pre(new Region(), node -> HBox.setHgrow(node, Priority.ALWAYS)));

        Button configBtn = new Button("配置");
        configBtn.setOnAction(e -> {
            Dialog<ConfigView> configViewDialog = new Dialog<>();
            configViewDialog.initOwner(stage);
            configViewDialog.setDialogPane(ConfigView.build());
            configViewDialog.setTitle("配置");
            configViewDialog.setResizable(true);
            configViewDialog.show();
        });
        headerBox.getChildren().add(configBtn);

        Button aboutBtn = new Button("关于");
        headerBox.getChildren().add(aboutBtn);
        headerBox.setPadding(new Insets(3, 8, 3, 8));
        headerBox.setAlignment(Pos.CENTER);

        TitledPane titledPane = new TitledPane("选项", headerBox);
        titledPane.setCollapsible(false);
        return titledPane;
    }

    protected void selectedLink(TreeView<TreeNode> originTree, TreeView<TreeNode> targetTree) {
        TreeItem<TreeNode> originItem = originTree.getSelectionModel().getSelectedItem();
        if (originItem == null) {
            return;
        }
        TreeNode appNode = originItem.getValue();
        if (appNode.getMappedNode() == null) {
            return;
        }
        TreeItem<TreeNode> targetItem = appNode.getMappedNode().getTreeItem();
        targetTree.getSelectionModel().select(targetItem);
        int targetItemIndex = targetTree.getRow(targetItem);
        targetTree.scrollTo(targetItemIndex);
    }

    protected void buildAppTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem markDelMenuItem = new MenuItem();
        markDelMenuItem.setOnAction(e -> {
            TreeItem<TreeNode> selectedItem = appTree.getSelectionModel().getSelectedItem();
            FileNode selectedNode = (FileNode) selectedItem.getValue();
            selectedNode.setStatus(selectedNode.getStatus() == FileNodeStatus.NONE ? FileNodeStatus.MARK_DEL : FileNodeStatus.NONE);
        });
        contextMenu.getItems().add(markDelMenuItem);
        contextMenu.setOnShowing(e -> {
            TreeItem<TreeNode> selectedItem = appTree.getSelectionModel().getSelectedItem();
            TreeNode selectedNode = selectedItem.getValue();
            if (selectedNode instanceof FileNode) {
                FileNodeStatus status = ((FileNode) selectedNode).getStatus();
                if (status == FileNodeStatus.NONE) {
                    markDelMenuItem.setText("标记删除");
                    markDelMenuItem.setVisible(true);
                } else if (status == FileNodeStatus.MARK_DEL) {
                    markDelMenuItem.setText("取消标记删除");
                    markDelMenuItem.setVisible(true);
                } else {
                    markDelMenuItem.setVisible(false);
                }
            } else {
                markDelMenuItem.setVisible(false);
            }
        });
        appTree.setContextMenu(contextMenu);
    }

    protected void refreshAppTree(File file) {
        TreeItem<TreeNode> rootItem = appTree.getRoot();
        appTreeInfo = AppPackService.getInstance().getTreeNode(file);
        rootItem.getChildren().clear();
        TreeNodeUtil.buildNode(rootItem, appTreeInfo.getRootNode(), OnlyChangeFilter.INSTANCE);
        appPathTextField.setText(file.getPath());
        Configuration.getInstance().setLastAppPackPath(file.getPath());
    }

    protected void buildAppTree() {
        TreeItem<TreeNode> rootItem = new TreeItem<>();
        appTree = new TreeView<>(rootItem);
        appTree.setCellFactory(treeView -> new FileTreeCell<>(this));
        buildAppTreeContextMenu();
        appTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            selectedLink(appTree, patchTree);
        });
        appTree.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TreeItem<TreeNode> selectedItem = appTree.getSelectionModel().getSelectedItem();
                TreeNode selectedTreeNode = selectedItem.getValue();
                if (!selectedTreeNode.getName().endsWith(FileExtConst.DOT_JAR)) {
                    return;
                }
                if (AppPackService.getInstance().buildNodeChildrenWithZip(selectedTreeNode)) {
                    TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode, OnlyChangeFilter.INSTANCE);
                }
            }
        });
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

    protected Node buildLeftCentre() {
        HBox appPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        appPackPathBox.getChildren().add(FXUtil.pre(appPathTextField = new TextField(), node -> {
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
        }));
        appPackPathBox.getChildren().add(new Label("应用包:"));
        appPackPathBox.getChildren().add(FXUtil.pre(new Button("选择"), node -> {
            node.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                if (!appPathTextField.getText().isBlank()) {
                    fileChooser.setInitialFileName(appPathTextField.getText());
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
        VBox.setVgrow(appTree, Priority.ALWAYS);
        return new VBox(appPackPathBox, appTree);
    }

    protected void buildPatchTreeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem markRootMenuItem = new MenuItem();
        markRootMenuItem.setOnAction(e -> {
            TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
            TreeNode customRootNode = patchTreeInfo.getCustomRootNode();
            if (Objects.equals(customRootNode, selectedItem.getValue())) {
                patchTreeInfo.setCustomRootNode(null);
            } else {
                patchTreeInfo.setCustomRootNode(selectedItem.getValue());
            }
            PatchPackService patchPackService = PatchPackService.getInstance();
            patchPackService.refreshReadmeNode(patchTreeInfo);
            patchPackService.refreshPatchMappedNode(appTreeInfo, patchTreeInfo);
            readMeTextArea.setText(patchTreeInfo.getReadMeText());
            patchTree.refresh();
        });
        contextMenu.getItems().addAll(markRootMenuItem);
        contextMenu.setOnShowing(e -> {
            TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
            TreeNode selectedNode = selectedItem.getValue();
            if (selectedNode instanceof ZipEntryNode && ((ZipEntryNode) selectedNode).getEntry().isDirectory()) {
                markRootMenuItem.setVisible(true);
                if (Objects.equals(selectedNode, patchTreeInfo.getCustomRootNode())) {
                    markRootMenuItem.setText("取消根节点标记");
                } else {
                    markRootMenuItem.setText("标记为根节点");
                }
            } else {
                markRootMenuItem.setVisible(false);
            }
        });
        patchTree.setContextMenu(contextMenu);
    }

    protected void refreshPatchTree(File file) {
        TreeItem<TreeNode> rootItem = patchTree.getRoot();
        PatchPackService patchPackService = PatchPackService.getInstance();
        patchTreeInfo = patchPackService.getTreeNode(file);
        patchPackService.refreshReadmeNode(patchTreeInfo);
        rootItem.getChildren().clear();
        TreeNodeUtil.buildNode(rootItem, patchTreeInfo.getRootNode());
        patchPackService.refreshPatchMappedNode(appTreeInfo, patchTreeInfo);
        TreeNodeUtil.expandedAllNode(rootItem);
        patchPathTextField.setText(file.getPath());
        Configuration.getInstance().setLastPatchPackPath(file.getPath());
        if (readMeTextArea != null) {
            readMeTextArea.setText(patchTreeInfo.getReadMeText());
        }
    }

    protected void buildPatchTree() {
        TreeItem<TreeNode> rootItem = new CheckBoxTreeItem<>();
        patchTree = new TreeView<>(rootItem);
        patchTree.setCellFactory(v -> new FileCheckBoxTreeCell<>(this));
        buildPatchTreeContextMenu();
        patchTree.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            selectedLink(patchTree, appTree);
        });
        patchTree.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TreeItem<TreeNode> selectedItem = patchTree.getSelectionModel().getSelectedItem();
                TreeNode selectedTreeNode = selectedItem.getValue();
                if (!selectedTreeNode.getName().endsWith(FileExtConst.DOT_ZIP)) {
                    return;
                }
                if (PatchPackService.getInstance().buildNodeChildrenWithZip(selectedTreeNode)) {
                    TreeNodeUtil.buildNodeChildren(selectedItem, selectedTreeNode);
                }
            }
        });
        String lastPatchPackPath = Configuration.getInstance().getLastPatchPackPath();
        if (lastPatchPackPath == null || lastPatchPackPath.isBlank()) {
            return;
        }
        File file = FileUtil.getExistsFile(lastPatchPackPath);
        if (file == null) {
            return;
        }
        refreshPatchTree(file);
    }

    protected Node buildRightCentre() {
        HBox patchPackPathBox = FXUtil.pre(new HBox(), node -> {
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(3, 8, 3, 8));
            node.setSpacing(3);
        });
        patchPackPathBox.getChildren().add(new Label("补丁包:"));
        patchPackPathBox.getChildren().add(FXUtil.pre(patchPathTextField = new TextField(), node -> {
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
        }));
        patchPackPathBox.getChildren().add(FXUtil.pre(new Button("选择"), node -> {
            node.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                if (!patchPathTextField.getText().isBlank()) {
                    fileChooser.setInitialFileName(patchPathTextField.getText());
                }
                fileChooser.setTitle("选择补丁包");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("补丁包", "*.zip"));
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    refreshPatchTree(file);
                }
            });
        }));
        buildPatchTree();
        VBox.setVgrow(patchTree, Priority.ALWAYS);
        return new VBox(patchPackPathBox, patchTree);
    }

    protected Node buildBottomCentre() {
        readMeTextArea = new TextArea();
        readMeTextArea.setEditable(false);
        if (patchTreeInfo != null) {
            readMeTextArea.setText(patchTreeInfo.getReadMeText());
        }
        TitledPane titledPane = new TitledPane("补丁信息", readMeTextArea);
        titledPane.setCollapsible(false);
        return titledPane;
    }

    protected Node buildCentre() {
        SplitPane topPane = new SplitPane(buildLeftCentre(), buildRightCentre());
        SplitPane centrePane = new SplitPane(topPane, buildBottomCentre());
        centrePane.setOrientation(Orientation.VERTICAL);
        centrePane.setDividerPositions(0.7);
        return centrePane;
    }

    protected Node buildFooter() {
        Label generateDetailLbl = new Label("新增：2 更新：3 删除：4 标记删除：5");
        generateDetailLbl.setTextFill(Color.RED);
        generateDetailLbl.setStyle("-fx-font-weight: bold;");
        HBox footerBox = new HBox(
                generateDetailLbl,
                FXUtil.pre(new Region(), node -> HBox.setHgrow(node, Priority.ALWAYS)),
                FXUtil.pre(new Button("保存"), btn -> {
                    btn.setOnAction(e -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("保存应用包");
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("应用包", "*.jar"));
                        File file = fileChooser.showSaveDialog(stage);
                        if (file == null) {
                            return;
                        }
                        AppPackService.getInstance().savePack(file, appTreeInfo);
                    });
                })
        );
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setSpacing(3);
        footerBox.setPadding(new Insets(5, 3, 0, 3));
        return footerBox;
    }

    public Pane build() {
        BorderPane rootPane = new BorderPane();
        rootPane.setPadding(new Insets(1, 2, 4, 2));
        rootPane.setTop(buildHeader());
        rootPane.setCenter(buildCentre());
        rootPane.setBottom(buildFooter());
        return rootPane;
    }
}