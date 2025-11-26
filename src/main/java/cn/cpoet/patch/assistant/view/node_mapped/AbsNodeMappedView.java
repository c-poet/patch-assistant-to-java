package cn.cpoet.patch.assistant.view.node_mapped;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.SpringConst;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @author CPoet
 */
public abstract class AbsNodeMappedView {

    protected String delInfo;
    protected String mappedInfo;
    protected TextArea textArea;
    protected final TreeNode appRootNode;
    protected final TreeNode patchRootNode;
    protected final boolean isGenMappedInfo;

    protected AbsNodeMappedView(TreeNode appRootNode, TreeNode patchRootNode, boolean isGenMappedInfo) {
        this.appRootNode = appRootNode;
        this.patchRootNode = patchRootNode;
        this.isGenMappedInfo = isGenMappedInfo;
    }

    private static final class JarVerPatternHolder {
        private static final Pattern JAR_VER_PATTERN = Pattern.compile("-[0-9.]+-.*");
    }

    private static Pattern getJarVerPattern() {
        return JarVerPatternHolder.JAR_VER_PATTERN;
    }

    protected TextArea createTextArea() {
        TextArea textArea = new TextArea();
        textArea.setPadding(Insets.EMPTY);
        return textArea;
    }

    protected Node build() {
        textArea = createTextArea();
        ContextMenu contentMenu = createContentMenu();
        if (contentMenu != null) {
            textArea.setContextMenu(contentMenu);
        }
        if (isGenMappedInfo) {
            buildMappedInfo();
        }
        return textArea;
    }

    protected ContextMenu createContentMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem(I18nUtil.t("app.common.copy"));
        copyItem.setOnAction(e -> textArea.copy());
        contextMenu.getItems().add(copyItem);
        if (textArea.isEditable()) {
            MenuItem pasteItem = new MenuItem(I18nUtil.t("app.common.paste"));
            pasteItem.setOnAction(e -> textArea.paste());
            MenuItem cutItem = new MenuItem(I18nUtil.t("app.common.cut"));
            cutItem.setOnAction(e -> textArea.cut());
            contextMenu.getItems().addAll(pasteItem, cutItem);
        }
        MenuItem selectAllItem = new MenuItem(I18nUtil.t("app.common.select-all"));
        selectAllItem.setOnAction(e -> textArea.selectAll());
        contextMenu.getItems().add(selectAllItem);
        if (isGenMappedInfo) {
            RadioMenuItem includeDelItem = new RadioMenuItem(I18nUtil.t("app.view.node-mapped.include-delete"));
            includeDelItem.setSelected(true);
            includeDelItem.setOnAction(e -> updateText(includeDelItem.isSelected()));
            contextMenu.getItems().addAll(new SeparatorMenuItem(), includeDelItem);
        }
        return contextMenu;
    }

    private void updateText(boolean appendDel) {
        UIUtil.runNotUI(() -> {
            if (appendDel && delInfo == null) {
                buildDelInfo();
            }
            if (!appendDel || StringUtil.isBlank(delInfo)) {
                updateText(mappedInfo);
                return;
            }
            if (StringUtil.isBlank(mappedInfo)) {
                updateText(delInfo);
            } else {
                updateText(delInfo + '\n' + mappedInfo);
            }
        });
    }

    private void updateText(String text) {
        UIUtil.runNotUI(() -> UIUtil.runUI(() -> textArea.setText(text)));
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
        UIUtil.runNotUI(() -> {
            StringBuilder sb = new StringBuilder();
            buildMappedInfo(sb, patchRootNode);
            mappedInfo = sb.toString();
            updateText(true);
        });
    }

    private void buildMappedInfo(StringBuilder sb, TreeNode rootNode) {
        if (CollectionUtil.isNotEmpty(rootNode.getChildren())) {
            Stack<String> pathStack = new Stack<>();
            rootNode.getChildren().forEach(node -> buildMappedInfo(sb, node, pathStack));
        }
    }

    private String removeJarVersionInfo(String name) {
        return getJarVerPattern().matcher(name).replaceAll("");
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
                    // jar包的情况下，去除版本号
                    if (parent.getName().endsWith(FileExtConst.DOT_JAR)) {
                        paths.add(removeJarVersionInfo(parent.getName()));
                    } else {
                        paths.add(parent.getName());
                    }
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

    protected void handleCopyInfo() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(textArea.getText());
        systemClipboard.setContent(content);
    }

    protected String getSaveFileName() {
        String readmeFile = Configuration.getInstance().getPatch().getReadmeFile();
        return StringUtil.isBlank(readmeFile) ? AppConst.README_FILE : readmeFile;
    }

    protected void handleSaveAsFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(getSaveFileName());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("README FILE", "*" + FileExtConst.DOT_TXT));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            FileUtil.writeFile(file, textArea.getText().getBytes());
        }
    }
}
