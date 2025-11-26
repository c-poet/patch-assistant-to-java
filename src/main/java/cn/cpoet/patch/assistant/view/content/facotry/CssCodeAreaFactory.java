package cn.cpoet.patch.assistant.view.content.facotry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CPoet
 */
public class CssCodeAreaFactory extends CodeAreaFactory {

    private static final String[] CSS_KEYWORDS = new String[]{
            "align-content", "align-items", "align-self", "all", "animation", "animation-delay",
            "animation-direction", "animation-duration", "animation-fill-mode", "animation-iteration-count"
    };

    private static final String CSS_PATTERN = String.format(
            "(?<KEYWORD>%s)|(?<SELECTOR>\\.[a-zA-Z][a-zA-Z0-9-]*|#[a-zA-Z][a-zA-Z0-9-]*|:[a-zA-Z][a-zA-Z0-9-]*|\\[[a-zA-Z][a-zA-Z0-9-]*\\])|" +
                    "(?<PROPERTY>[a-zA-Z-]+)(?<PUNCTUATION>\\s*:\\s*)(?<VALUE>[^;]+)(?<TERMINATOR>;)|" +
                    "(?<COMMENT>/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/)",
            String.join("|", CSS_KEYWORDS));

    private static final Pattern PATTERN = Pattern.compile(CSS_PATTERN);

    @Override
    public String getStyleSheetPath() {
        return "/css/css-style.css";
    }

    @Override
    protected Matcher createMatcher(String text) {
        return PATTERN.matcher(text);
    }

    @Override
    protected String getStyleClass(Matcher matcher) {
        String styleClass = null;
        if (matcher.group("KEYWORD") != null) {
            styleClass = "keyword";
        } else if (matcher.group("SELECTOR") != null) {
            styleClass = "selector";
        } else if (matcher.group("PROPERTY") != null) {
            styleClass = "property";
        } else if (matcher.group("PUNCTUATION") != null) {
            styleClass = "punctuation";
        } else if (matcher.group("VALUE") != null) {
            styleClass = "value";
        } else if (matcher.group("TERMINATOR") != null) {
            styleClass = "terminator";
        } else if (matcher.group("COMMENT") != null) {
            styleClass = "comment";
        }
        return styleClass;
    }
}
