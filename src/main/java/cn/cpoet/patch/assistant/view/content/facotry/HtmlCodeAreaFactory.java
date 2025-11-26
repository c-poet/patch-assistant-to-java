package cn.cpoet.patch.assistant.view.content.facotry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CPoet
 */
public class HtmlCodeAreaFactory extends CodeAreaFactory {

    private static final String HTML_TAG_PATTERN = "(</?)([a-zA-Z][a-zA-Z0-9-]*)([^>]*>)";
    private static final String HTML_ATTRIBUTE_PATTERN = "\\s([a-zA-Z-]+)(=\"[^\"]*\")?";
    private static final String HTML_COMMENT_PATTERN = "<!--.*?-->";
    private static final String HTML_STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String HTML_DOCTYPE_PATTERN = "<!DOCTYPE.*?>";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<TAG>" + HTML_TAG_PATTERN + ")" +
                    "|(?<ATTRIBUTE>" + HTML_ATTRIBUTE_PATTERN + ")" +
                    "|(?<COMMENT>" + HTML_COMMENT_PATTERN + ")" +
                    "|(?<STRING>" + HTML_STRING_PATTERN + ")" +
                    "|(?<DOCTYPE>" + HTML_DOCTYPE_PATTERN + ")"
    );

    @Override
    public String getStyleSheetPath() {
        return "/css/html-style.css";
    }

    @Override
    protected Matcher createMatcher(String text) {
        return PATTERN.matcher(text);
    }

    @Override
    protected String getStyleClass(Matcher matcher) {
        String styleClass = null;
        if (matcher.group("TAG") != null) {
            styleClass = "html-tag";
        } else if (matcher.group("ATTRIBUTE") != null) {
            styleClass = "html-attribute";
        } else if (matcher.group("COMMENT") != null) {
            styleClass = "html-comment";
        } else if (matcher.group("STRING") != null) {
            styleClass = "html-string";
        } else if (matcher.group("DOCTYPE") != null) {
            styleClass = "html-doctype";
        }
        return styleClass;
    }
}
