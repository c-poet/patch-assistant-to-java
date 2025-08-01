package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.SpringConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import cn.cpoet.patch.assistant.view.tree.TreeNodeType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * 查看最新绑定的信息
 *
 * @author CPoet
 */
public class NodeMappedView {

    private String delInfo;
    private String mappedInfo;
    private TextArea textArea;
    private final TreeNode appRootNode;
    private final TreeNode patchRootNode;

    public NodeMappedView(TreeNode appRootNode, TreeNode patchRootNode) {
        this.appRootNode = appRootNode;
        this.patchRootNode = patchRootNode;
    }

    private Node build() {
        buildMappedInfo();
        VBox box = new VBox();
        box.setPadding(Insets.EMPTY);
        textArea = new TextArea();
        textArea.setEditable(false);
        ToolBar toolBar = new ToolBar();
        CheckBox diffModeCheckBox = new CheckBox("包含删除节点");
        diffModeCheckBox.setSelected(true);
        diffModeCheckBox.setOnAction(e -> updateText(textArea, diffModeCheckBox.isSelected()));
        toolBar.getItems().add(diffModeCheckBox);
        box.getChildren().add(toolBar);
        box.getChildren().add(textArea);
        updateText(textArea, true);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        return box;
    }

    private void updateText(TextArea textArea, boolean appendDel) {
        if (appendDel && delInfo == null) {
            buildDelInfo();
        }
        if (!appendDel || StringUtil.isBlank(delInfo)) {
            textArea.setText(mappedInfo);
            return;
        }
        if (StringUtil.isBlank(mappedInfo)) {
            textArea.setText(delInfo);
        } else {
            textArea.setText(mappedInfo + '\n' + delInfo);
        }
    }

    private void buildDelInfo() {
        StringBuilder sb = new StringBuilder();
        buildDelInfo(sb, appRootNode);
        delInfo = sb.toString();
    }

    private void buildDelInfo(StringBuilder sb, TreeNode rootNode) {
        if (CollectionUtil.isNotEmpty(rootNode.getChildren())) {
            Stack<String> patchStack = new Stack<>();
            rootNode.getChildren().forEach(node -> buildDelInfo(sb, node, patchStack, false, false));
        }
    }

    private void buildDelInfo(StringBuilder sb, TreeNode node, Stack<String> pathStack, boolean isLib, boolean isClasses) {
        if (TreeNodeType.DEL.equals(node.getType())) {
            if (!StringUtil.isEmpty(sb)) {
                sb.append('\n');
            }
            sb.append('-').append(node.getName());
            if (isLib) {
                sb.append('\t').append(String.join(FileNameUtil.SEPARATOR, pathStack.subList(0, 3)));
                if (pathStack.size() > 3) {
                    sb.append('\t').append(String.join(FileNameUtil.SEPARATOR, pathStack.subList(3, pathStack.size())));
                }
            } else if (isClasses) {
                sb.append('\t').append(String.join(FileNameUtil.SEPARATOR, pathStack.subList(0, 2)));
                if (pathStack.size() > 2) {
                    sb.append('\t').append(String.join(FileNameUtil.SEPARATOR, pathStack.subList(2, pathStack.size())));
                }
            } else if (!pathStack.isEmpty()) {
                sb.append('\t').append(String.join(FileNameUtil.SEPARATOR, pathStack));
            } else {
                sb.append('\t').append(FileNameUtil.SEPARATOR);
            }
            return;
        }
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            if (node.isDir()) {
                pathStack.push(node.getName());
                if (isLib || isClasses) {
                    node.getChildren().forEach(child -> buildDelInfo(sb, child, pathStack, isLib, isClasses));
                } else {
                    boolean isLibFlag = SpringConst.LIB_PATH.equals(node.getPath());
                    boolean isClassesFlag = SpringConst.LIB_PATH.equals(node.getPath());
                    node.getChildren().forEach(child -> buildDelInfo(sb, child, pathStack, isLibFlag, isClassesFlag));
                }
                pathStack.pop();
            } else if (node.getName().endsWith(FileExtConst.DOT_JAR)) {
                pathStack.push(node.getName());
                node.getChildren().forEach(child -> buildDelInfo(sb, child, pathStack, isLib, isClasses));
                pathStack.pop();
            } else {
                node.getChildren().forEach(child -> buildDelInfo(sb, child, pathStack, isLib, isClasses));
            }
        }
    }

    private void buildMappedInfo() {
        StringBuilder sb = new StringBuilder();
        buildMappedInfo(sb, patchRootNode);
        mappedInfo = sb.toString();
    }

    private void buildMappedInfo(StringBuilder sb, TreeNode rootNode) {
        if (CollectionUtil.isNotEmpty(rootNode.getChildren())) {
            Stack<String> pathStack = new Stack<>();
            rootNode.getChildren().forEach(node -> buildMappedInfo(sb, node, pathStack));
        }
    }

    private void buildMappedInfo(StringBuilder sb, TreeNode node, Stack<String> pathStack) {
        if (node.isDir()) {
            if (CollectionUtil.isNotEmpty(node.getChildren())) {
                pathStack.push(node.getName());
                node.getChildren().forEach(child -> buildMappedInfo(sb, child, pathStack));
                pathStack.pop();
            }
            return;
        }
        if (node.getMappedNode() == null) {
            return;
        }
        if (!StringUtil.isEmpty(sb)) {
            sb.append('\n');
        }
        String path = CollectionUtil.isEmpty(pathStack) ? node.getName() : String.join(FileNameUtil.SEPARATOR, pathStack)
                + FileNameUtil.SEPARATOR + node.getName();
        if (TreeNodeType.ADD.equals(node.getType())) {
            sb.append("+");
        } else {
            sb.append("!");
        }
        sb.append(path);
        String secondPath = null;
        List<String> paths = new ArrayList<>();
        TreeNode parent = node.getMappedNode().getParent();
        while (parent != null && !TreeNodeType.ROOT.equals(parent.getType())) {
            TreeNode nextParent = parent.getParent();
            if (nextParent != null && CollectionUtil.isNotEmpty(paths)) {
                if (SpringConst.LIB_PATH.equals(nextParent.getPath())) {
                    Collections.reverse(paths);
                    secondPath = String.join(FileNameUtil.SEPARATOR, paths);
                    paths.clear();
                    paths.add(parent.getName());
                } else if (SpringConst.CLASSES_PATH.equals(nextParent.getPath())) {
                    paths.add(parent.getName());
                    Collections.reverse(paths);
                    secondPath = String.join(FileNameUtil.SEPARATOR, paths);
                    paths.clear();
                } else {
                    paths.add(parent.getName());
                }
            } else {
                paths.add(parent.getName());
            }
            parent = nextParent;
        }
        if (CollectionUtil.isNotEmpty(paths)) {
            Collections.reverse(paths);
            sb.append('\t').append(String.join(FileNameUtil.SEPARATOR, paths));
        }
        if (!StringUtil.isBlank(secondPath)) {
            sb.append('\t').append(secondPath);
        }
    }

    private void handleCopyInfo() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(textArea.getText());
        systemClipboard.setContent(content);
    }

    private void handleSaveAsFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        String readmeFile = Configuration.getInstance().getPatch().getReadmeFile();
        fileChooser.setInitialFileName(StringUtil.isBlank(readmeFile) ? AppConst.README_FILE : readmeFile);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            FileUtil.writeFile(file, textArea.getText().getBytes());
        }
    }

    public void showDialog(Stage stage) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.NONE);
        dialog.setResizable(true);
        dialog.setTitle(I18nUtil.t("app.view.node-mapped.title"));
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(build());
        Configuration configuration = Configuration.getInstance();
        dialogPane.setPrefSize(configuration.getNodeMappedWidth(), configuration.getNodeMappedHeight());
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setNodeMappedWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setNodeMappedHeight(newVal.doubleValue()));
        dialog.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().add(new ButtonType(I18nUtil.t("app.view.node-mapped.save-as-file"), ButtonBar.ButtonData.YES));
        dialogPane.getButtonTypes().add(new ButtonType(I18nUtil.t("app.view.node-mapped.copy-info"), ButtonBar.ButtonData.OK_DONE));
        dialogPane.getButtonTypes().add(ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
                handleCopyInfo();
            } else if (buttonType.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                handleSaveAsFile(stage);
            }
            return Boolean.TRUE;
        });
        dialog.showAndWait();
    }
}
