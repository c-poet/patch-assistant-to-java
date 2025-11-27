package cn.cpoet.patch.assistant.view.node_mapped;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.ChangeTypeEnum;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.StyleConst;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
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
    protected CodeArea codeArea;
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
        String codeAreaStylePath = FileUtil.getResourceAndExternalForm(StyleConst.STYLE_FILE_CODE_AREA);
        if (codeAreaStylePath != null) {
            codeArea.getStylesheets().add(codeAreaStylePath);
        }
        codeAreaStylePath = FileUtil.getResourceAndExternalForm(StyleConst.STYLE_FILE_README);
        if (codeAreaStylePath != null) {
            codeArea.getStylesheets().add(codeAreaStylePath);
        }
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        return codeArea;
    }

    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        Map<Integer, ChangeTypeEnum> lineChangeTypeMap = ReadMeFileService.INSTANCE.getTextLineChangeType(text);
        if (CollectionUtil.isEmpty(lineChangeTypeMap)) {
            return null;
        }
        int start = 0, end = 0, lineNo = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (end < text.length()) {
            if (text.charAt(end) == '\n') {
                ++lineNo;
                addLineStyle(lineNo, start, end, spansBuilder, lineChangeTypeMap);
                start = end;
            }
            ++end;
        }
        if (start != end) {
            addLineStyle(++lineNo, start, end, spansBuilder, lineChangeTypeMap);
        }
        return spansBuilder.create();
    }

    private void addLineStyle(int lineNo, int start, int end, StyleSpansBuilder<Collection<String>> spansBuilder,
                              Map<Integer, ChangeTypeEnum> lineChangeTypeMap) {
        String lineStyleClass = getLineStyleClass(lineChangeTypeMap, lineNo);
        if (StringUtil.isBlank(lineStyleClass)) {
            spansBuilder.add(Collections.singleton(lineStyleClass), end - start);
        } else {
            spansBuilder.add(Collections.singleton(lineStyleClass), end - start);
        }
    }

    private String getLineStyleClass(Map<Integer, ChangeTypeEnum> lineChangeTypeMap, int line) {
        ChangeTypeEnum changeTypeEnum = lineChangeTypeMap.get(line);
        if (changeTypeEnum == null) {
            return null;
        }
        return getLineStyleClass(changeTypeEnum);
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
        codeArea = createTextArea();
        ContextMenu contentMenu = createContentMenu();
        if (contentMenu != null) {
            codeArea.setContextMenu(contentMenu);
        }
        if (isGenMappedInfo) {
            buildMappedInfo();
        }
        return codeArea;
    }

    protected ContextMenu createContentMenu() {
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
        UIUtil.runNotUI(() -> UIUtil.runUI(() -> codeArea.replaceText(text)));
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
        content.putString(codeArea.getText());
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
            FileUtil.writeFile(file, codeArea.getText().getBytes());
        }
    }
}
