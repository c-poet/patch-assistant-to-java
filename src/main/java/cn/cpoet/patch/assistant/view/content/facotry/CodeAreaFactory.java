package cn.cpoet.patch.assistant.view.content.facotry;

import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.ContentConf;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * {@link  org.fxmisc.richtext.CodeArea}工厂
 *
 * @author CPoet
 */
public abstract class CodeAreaFactory {

    protected static final String CODE_AREA_CSS = FileUtil.getResourceAndExternalForm("/css/code-area.css");

    public static final EventType<?> SHOW_MODE_CHANGE = new EventType<>("SHOW_MODE_CHANGE");
    public static final EventType<?> EDIT_MODE_CHANGE = new EventType<>("EDIT_MODE_CHANGE");
    public static final EventType<CharsetChangeEvent> CHARSET_CHANGE = new EventType<>("CHARSET_CHANGE");

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

    private void emitCharsetChange(CodeArea codeArea, String charset) {
        CharsetChangeEvent charsetChangeEvent = new CharsetChangeEvent(CHARSET_CHANGE);
        charsetChangeEvent.setCharset(charset);
        codeArea.fireEvent(charsetChangeEvent);
    }

    private ContextMenu getContextMenu(CodeArea codeArea, boolean hasDiffMode) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem copyItem = new MenuItem(I18nUtil.t("app.view.content.copy"));
        copyItem.setOnAction(e -> codeArea.copy());
        contextMenu.getItems().add(copyItem);

        MenuItem selectedAllItem = new MenuItem(I18nUtil.t("app.view.content.select-all"));
        selectedAllItem.setOnAction(e -> codeArea.selectAll());
        contextMenu.getItems().add(selectedAllItem);
        MenuItem unSelectedItem = new MenuItem(I18nUtil.t("app.view.content.deselect"));
        unSelectedItem.setOnAction(e -> codeArea.deselect());
        contextMenu.getItems().add(unSelectedItem);

        Menu charsetMenu = new Menu(I18nUtil.t("app.view.content.charset"));
        ToggleGroup charsetGroup = new ToggleGroup();
        RadioMenuItem gbkItem = new RadioMenuItem("GBK");
        gbkItem.setToggleGroup(charsetGroup);
        gbkItem.setOnAction(e -> emitCharsetChange(codeArea, "GBK"));
        RadioMenuItem utf8Item = new RadioMenuItem("UTF-8");
        utf8Item.setToggleGroup(charsetGroup);
        utf8Item.setOnAction(e -> emitCharsetChange(codeArea, "UTF-8"));
        charsetMenu.getItems().add(gbkItem);
        charsetMenu.getItems().add(utf8Item);
        contextMenu.getItems().add(charsetMenu);
        charsetGroup.selectToggle(utf8Item);
        if (hasDiffMode) {
            RadioMenuItem diffModeItem = new RadioMenuItem(I18nUtil.t("app.view.content.diff-show-mode"));
            diffModeItem.setSelected(Boolean.TRUE.equals(Configuration.getInstance().getContent().getDiffModel()));
            diffModeItem.setOnAction(e -> {
                ContentConf content = Configuration.getInstance().getContent();
                content.setDiffModel(!Boolean.TRUE.equals(content.getDiffModel()));
                codeArea.fireEvent(new Event(SHOW_MODE_CHANGE));
            });
            contextMenu.getItems().add(diffModeItem);
        }/* else {
            RadioMenuItem editModeItem = new RadioMenuItem(I18nUtil.t("app.view.content.edit-mode"));
            editModeItem.setSelected(false);
            editModeItem.setOnAction(e -> codeArea.fireEvent(new Event(EDIT_MODE_CHANGE)));
            contextMenu.getItems().add(editModeItem);
        }*/
        contextMenu.setOnShowing(e -> {
            copyItem.setVisible(false);
            unSelectedItem.setVisible(false);
            selectedAllItem.setVisible(false);
            String selectedText = codeArea.getSelectedText();
            if (!StringUtil.isEmpty(selectedText)) {
                copyItem.setVisible(true);
                unSelectedItem.setVisible(true);
            }
            if (!Objects.equals(selectedText, codeArea.getText())) {
                selectedAllItem.setVisible(true);
            }
        });
        return contextMenu;
    }

    /**
     * 创建代码域
     *
     * @param hasDiffMode 是否存在比较模式
     * @return 代码域
     */
    public NodeCodeArea create(boolean hasDiffMode) {
        NodeCodeArea codeArea = new NodeCodeArea();
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
        ContextMenu contextMenu = getContextMenu(codeArea, hasDiffMode);
        codeArea.setContextMenu(contextMenu);
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
