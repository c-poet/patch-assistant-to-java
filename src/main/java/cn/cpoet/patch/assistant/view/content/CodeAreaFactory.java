package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;

/**
 * {@link  org.fxmisc.richtext.CodeArea}工厂
 *
 * @author CPoet
 */
public abstract class CodeAreaFactory {

    protected static final String CODE_AREA_CSS = FileUtil.getResourceAndExternalForm("/css/code-area.css");

    /**
     * 获取样式表路径
     *
     * @return 样式表路径
     */
    public String getStyleSheetPath() {
        return null;
    }

    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = createMatcher(text);
        if (matcher == null) {
            return null;
        }
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = getStyleClass(matcher);
            if (styleClass == null) {
                spansBuilder.add(Collections.emptyList(), matcher.end() - lastKwEnd);
            } else {
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            }
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private ContextMenu getContextMenu(CodeArea codeArea) {
        MenuItem copyItem = new MenuItem("复制");
        copyItem.setOnAction(e -> codeArea.copy());
        MenuItem selectedAllItem = new MenuItem("全选");
        selectedAllItem.setOnAction(e -> codeArea.selectAll());
        ContextMenu contextMenu = new ContextMenu(copyItem, selectedAllItem);
        contextMenu.setOnShowing(e -> {
            String selectedText = codeArea.getSelectedText();
            copyItem.setVisible(!StringUtil.isEmpty(selectedText));
        });
        return contextMenu;
    }

    /**
     * 创建代码域
     *
     * @return 代码域
     */
    public CodeArea create() {
        CodeArea codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.getStylesheets().addAll(CODE_AREA_CSS);
        String styleSheetPath = getStyleSheetPath();
        if (!StringUtil.isBlank(styleSheetPath)) {
            String stylesheetPath = FileUtil.getResourceAndExternalForm(styleSheetPath);
            codeArea.getStylesheets().add(stylesheetPath);
        }
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    StyleSpans<Collection<String>> styleSpans = computeHighlighting(codeArea.getText());
                    if (styleSpans != null) {
                        codeArea.setStyleSpans(0, styleSpans);
                    }
                });
        ContextMenu contextMenu = getContextMenu(codeArea);
        if (contextMenu != null) {
            codeArea.setContextMenu(contextMenu);
        }
        return codeArea;
    }

    protected Matcher createMatcher(String text) {
        return null;
    }

    protected String getStyleClass(Matcher matcher) {
        return null;
    }

    public static class DefaultCodeAreaFactory extends CodeAreaFactory {

    }
}
