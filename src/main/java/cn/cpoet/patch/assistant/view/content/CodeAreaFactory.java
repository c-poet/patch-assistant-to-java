package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

/**
 * {@link  org.fxmisc.richtext.CodeArea}工厂
 *
 * @author CPoet
 */
public abstract class CodeAreaFactory {

    /**
     * 获取样式表路径
     *
     * @return 样式表路径
     */
    public String getStyleSheetPath() {
        return null;
    }

    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        return null;
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
        return codeArea;
    }

    public static class DefaultCodeAreaFactory extends CodeAreaFactory {

    }
}
