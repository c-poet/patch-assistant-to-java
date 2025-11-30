package cn.cpoet.patch.assistant.view.node_mapped;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.ChangeTypeEnum;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.StyleConst;
import cn.cpoet.patch.assistant.control.code.CodeEditor;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.model.ReadMePathInfo;
import cn.cpoet.patch.assistant.service.ReadMeFileService;
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
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author CPoet
 */
public abstract class AbsNodeMappedView {

    protected String delInfo;
    protected String mappedInfo;
    protected CodeEditor codeEditor;
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

    protected CodeEditor createTextEditor() {
        CodeEditor codeEditor = new CodeEditor();
        codeEditor.setPadding(Insets.EMPTY);
        CodeArea codeArea = codeEditor.getCodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        String codeAreaStylePath = FileUtil.getResourceAndExternalForm(StyleConst.STYLE_FILE_CODE_AREA);
        if (codeAreaStylePath != null) {
            codeEditor.getStylesheets().add(codeAreaStylePath);
        }
        codeAreaStylePath = FileUtil.getResourceAndExternalForm(StyleConst.STYLE_FILE_README);
        if (codeAreaStylePath != null) {
            codeEditor.getStylesheets().add(codeAreaStylePath);
        }
        CodeEditor.applyHighlighting(codeArea, this::computeHighlighting);
        return codeEditor;
    }

    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        List<ReadMePathInfo> pathInfos = ReadMeFileService.INSTANCE.getPathInfos(text, patchRootNode, appRootNode);
        if (CollectionUtil.isEmpty(pathInfos)) {
            return null;
        }
        int index = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        for (ReadMePathInfo pathInfo : pathInfos) {
            if (index < pathInfo.getStartIndex()) {
                spansBuilder.add(Collections.emptyList(), pathInfo.getStartIndex() - index);
            }
            String lineStyleClass = getLineStyleClass(pathInfo.getType());
            if (StringUtil.isBlank(lineStyleClass)) {
                spansBuilder.add(Collections.emptyList(), pathInfo.getEndIndex() - pathInfo.getStartIndex());
            } else {
                spansBuilder.add(Collections.singleton(lineStyleClass), pathInfo.getEndIndex() - pathInfo.getStartIndex());
            }
            index = pathInfo.getEndIndex();
        }
        return spansBuilder.create();
    }

    private String getLineStyleClass(ChangeTypeEnum changeTypeEnum) {
        switch (changeTypeEnum) {
            case ADD:
                return "add";
            case MOD:
                return "mod";
            case DEL:
                return "del";
            case IGNORE:
                return "ignore";
            case NONE:
            default:
                return null;
        }
    }

    protected Node build() {
        codeEditor = createTextEditor();
        ContextMenu contentMenu = createContentMenu();
        if (contentMenu != null) {
            codeEditor.getCodeArea().setContextMenu(contentMenu);
        }
        if (isGenMappedInfo) {
            buildMappedInfo();
        }
        return codeEditor;
    }

    protected ContextMenu createContentMenu() {
        CodeArea codeArea = codeEditor.getCodeArea();
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem(I18nUtil.t("app.common.copy"));
        copyItem.setOnAction(e -> codeArea.copy());
        contextMenu.getItems().add(copyItem);
        if (codeArea.isEditable()) {
            MenuItem pasteItem = new MenuItem(I18nUtil.t("app.common.paste"));
            pasteItem.setOnAction(e -> codeArea.paste());
            MenuItem cutItem = new MenuItem(I18nUtil.t("app.common.cut"));
            cutItem.setOnAction(e -> codeArea.cut());
            contextMenu.getItems().addAll(pasteItem, cutItem);
        }
        MenuItem selectAllItem = new MenuItem(I18nUtil.t("app.common.select-all"));
        selectAllItem.setOnAction(e -> codeArea.selectAll());
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
        UIUtil.runNotUI(() -> UIUtil.runUI(() -> codeEditor.getCodeArea().replaceText(text)));
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
        content.putString(codeEditor.getCodeArea().getText());
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
            FileUtil.writeFile(file, codeEditor.getCodeArea().getText().getBytes());
        }
    }
}
