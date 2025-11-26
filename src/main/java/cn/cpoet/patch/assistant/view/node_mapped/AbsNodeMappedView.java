package cn.cpoet.patch.assistant.view.node_mapped;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.ChangeTypeEnum;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

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
    protected CodeArea textArea;
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

    protected CodeArea createTextArea() {
        CodeArea codeArea = new CodeArea();
        codeArea.setPadding(Insets.EMPTY);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setStyle("-fx-font-size: 12pt;");
        return codeArea;
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
            RadioMenuItem includeDelMenuItem = getIncludeDelMenuItem();
            if (includeDelMenuItem != null) {
                contextMenu.getItems().add(includeDelMenuItem);
            }
        }
        return contextMenu;
    }

    protected RadioMenuItem getIncludeDelMenuItem() {
        RadioMenuItem includeDelItem = new RadioMenuItem(I18nUtil.t("app.view.node-mapped.include-delete"));
        includeDelItem.setSelected(true);
        includeDelItem.setOnAction(e -> updateText(includeDelItem.isSelected()));
        return includeDelItem;
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

    protected void updateText(String text) {
        UIUtil.runNotUI(() -> UIUtil.runUI(() -> textArea.replaceText(text)));
    }

    private void buildDelInfo() {
        StringBuilder sb = new StringBuilder();
        buildDelInfo(sb, appRootNode);
        delInfo = sb.toString();
    }

    private void buildDelInfo(StringBuilder sb, TreeNode rootNode) {
        if (CollectionUtil.isNotEmpty(rootNode.getChildren())) {
            Stack<String> patchStack = new Stack<>();
            rootNode.getChildren().forEach(node -> buildDelInfo(sb, node, patchStack));
        }
    }

    private void buildDelInfo(StringBuilder sb, TreeNode node, Stack<String> pathStack) {
        if (TreeNodeType.DEL.equals(node.getType())) {
            if (!StringUtil.isEmpty(sb)) {
                sb.append('\n');
            }
            sb.append(ChangeTypeEnum.DEL.getCode());
            if (CollectionUtil.isNotEmpty(pathStack)) {
                sb.append(String.join(FileNameUtil.SEPARATOR, pathStack)).append(FileNameUtil.SEPARATOR);
            }
            sb.append(node.getName());
            return;
        }
        if (CollectionUtil.isNotEmpty(node.getChildren())) {
            if (node.isDir() || TreeNodeUtil.isCompressNode(node)) {
                pathStack.push(getNameWithoutVersion(node.getName()));
                node.getChildren().forEach(child -> buildDelInfo(sb, child, pathStack));
                pathStack.pop();
            } else {
                node.getChildren().forEach(child -> buildDelInfo(sb, child, pathStack));
            }
        }
    }

    protected void buildMappedInfo() {
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
        sb.append(TreeNodeType.ADD.equals(node.getType()) ? ChangeTypeEnum.ADD.getCode() : ChangeTypeEnum.MOD.getCode());
        if (CollectionUtil.isNotEmpty(pathStack)) {
            sb.append(String.join(FileNameUtil.SEPARATOR, pathStack)).append(FileNameUtil.SEPARATOR);
        }
        sb.append(node.getName());
        List<String> paths = new ArrayList<>();
        TreeNode parent = node.getMappedNode();
        while (parent != null && !TreeNodeType.ROOT.equals(parent.getType())) {
            paths.add(getNameWithoutVersion(parent.getName()));
            parent = parent.getParent();
        }
        if (CollectionUtil.isNotEmpty(paths)) {
            Collections.reverse(paths);
            sb.append('\t').append(String.join(FileNameUtil.SEPARATOR, paths));
        }
    }

    protected String getNameWithoutVersion(String name) {
        if (name.endsWith(FileExtConst.DOT_JAR)) {
            return removeJarVersionInfo(name);
        }
        return name;
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
