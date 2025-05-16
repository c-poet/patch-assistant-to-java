package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.view.tree.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HomeView {

    protected final Stage stage;

    public HomeView(Stage stage) {
        this.stage = stage;
    }

    protected Node buildHeader() {
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        CheckBox checkScrollLink = new CheckBox("文件联动");
        headerBox.getChildren().add(checkScrollLink);

        CheckBox checkDockerImage = new CheckBox("Docker镜像");
        headerBox.getChildren().add(checkDockerImage);

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        headerBox.getChildren().add(region);

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
        return headerBox;
    }

    protected Node buildLeftCentre() {
        HBox appPackPathBox = new HBox(
                new Label("应用包:")
        );
        appPackPathBox.setAlignment(Pos.CENTER);

        TextField appPackPathTextField = new TextField();
        appPackPathTextField.setEditable(false);
        HBox.setHgrow(appPackPathTextField, Priority.ALWAYS);
        appPackPathBox.getChildren().add(appPackPathTextField);

        TreeItem<TreeNode> rootItem = new TreeItem<>();
        TreeView<TreeNode> appPackTree = new TreeView<>(rootItem);
        appPackTree.setCellFactory(treeView -> new FileTreeCell<>());

        Button appPackPathBtn = new Button("选择");
        appPackPathBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            if (!appPackPathTextField.getText().isBlank()) {
                fileChooser.setInitialFileName(appPackPathTextField.getText());
            }
            fileChooser.setTitle("选择应用包");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("应用包", "*.jar"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                FileNode rootNode = new FileNode();
                rootNode.setName(file.getName());
                rootNode.setPath(file.getPath());
                rootItem.setValue(rootNode);
                appPackPathTextField.setText(file.getPath());
                try (InputStream in = new FileInputStream(file);
                     ZipInputStream jis = new ZipInputStream(in)) {
                    ZipEntry zipEntry;
                    Map<String, TreeItem<TreeNode>> treeItemMap = new HashMap<>();
                    while ((zipEntry = jis.getNextEntry()) != null) {
                        ZipEntryNode zipEntryNode = new ZipEntryNode();
                        zipEntryNode.setName(FileNameUtil.getFileName(zipEntry.getName()));
                        zipEntryNode.setPath(zipEntry.getName());
                        zipEntryNode.setEntry(zipEntry);
                        if (!zipEntry.isDirectory()) {
                            zipEntryNode.setBytes(jis.readAllBytes());
                        }
                        TreeItem<TreeNode> newItem = new TreeItem<>(zipEntryNode);
                        TreeItem<TreeNode> parentItem = treeItemMap.getOrDefault(FileNameUtil.getDirPath(zipEntry.getName()), rootItem);
                        parentItem.getChildren().add(newItem);
                        if (zipEntry.isDirectory()) {
                            treeItemMap.put(zipEntry.getName().substring(0, zipEntry.getName().length() - 1), newItem);
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        appPackPathBox.getChildren().add(appPackPathBtn);
        VBox.setVgrow(appPackTree, Priority.ALWAYS);
        return new VBox(appPackPathBox, appPackTree);
    }

    protected Node buildRightCentre() {
        TextField patchPackPathTextField = new TextField();
        HBox.setHgrow(patchPackPathTextField, Priority.ALWAYS);

        TreeItem<TreeNode> rootItem = new CheckBoxTreeItem<>();
        TreeView<TreeNode> patchPackTree = new TreeView<>(rootItem);
        patchPackTree.setCellFactory(v -> new FileCheckBoxTreeCell<>());

        Button patchPackPathBtn = new Button("选择");
        patchPackPathBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            if (!patchPackPathTextField.getText().isBlank()) {
                fileChooser.setInitialFileName(patchPackPathTextField.getText());
            }
            fileChooser.setTitle("选择补丁包");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("补丁包", "*.zip"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                FileNode rootNode = new FileNode();
                rootNode.setName(file.getName());
                rootNode.setPath(file.getPath());
                rootItem.setValue(rootNode);
                patchPackPathTextField.setText(file.getPath());

                try (InputStream in = new FileInputStream(file);
                     ZipInputStream jis = new ZipInputStream(in)) {
                    ZipEntry zipEntry;
                    Map<String, TreeItem<TreeNode>> treeItemMap = new HashMap<>();
                    while ((zipEntry = jis.getNextEntry()) != null) {
                        ZipEntryNode zipEntryNode = new ZipEntryNode();
                        zipEntryNode.setName(FileNameUtil.getFileName(zipEntry.getName()));
                        zipEntryNode.setPath(zipEntry.getName());
                        zipEntryNode.setEntry(zipEntry);
                        if (!zipEntry.isDirectory()) {
                            zipEntryNode.setBytes(jis.readAllBytes());
                        }
                        TreeItem<TreeNode> newItem = new CheckBoxTreeItem<>(zipEntryNode);
                        TreeItem<TreeNode> parentItem = treeItemMap.getOrDefault(FileNameUtil.getDirPath(zipEntry.getName()), rootItem);
                        parentItem.getChildren().add(newItem);
                        if (zipEntry.isDirectory()) {
                            treeItemMap.put(zipEntry.getName().substring(0, zipEntry.getName().length() - 1), newItem);
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        HBox patchPackPathBox = new HBox(
                new Label("补丁包:"),
                patchPackPathTextField,
                patchPackPathBtn
        );
        patchPackPathBox.setAlignment(Pos.CENTER);

        VBox.setVgrow(patchPackTree, Priority.ALWAYS);
        return new VBox(patchPackPathBox, patchPackTree);
    }

    protected Node buildCentre() {
        return new SplitPane(buildLeftCentre(), buildRightCentre());
    }

    protected Node buildFooter() {
        TextField outputPathTextField = new TextField();
        outputPathTextField.setEditable(false);
        HBox.setHgrow(outputPathTextField, Priority.ALWAYS);
        HBox footerBox = new HBox(
                new Label("路径:"),
                outputPathTextField,
                new Button("选择"),
                new Button("生成")
        );
        footerBox.setAlignment(Pos.CENTER);
        return footerBox;
    }

    public Pane build() {
        BorderPane rootPane = new BorderPane();
        rootPane.setTop(buildHeader());
        rootPane.setCenter(buildCentre());
        rootPane.setBottom(buildFooter());
        return rootPane;
    }
}