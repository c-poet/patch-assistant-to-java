package cn.cpoet.patch.assistant.view.content.facotry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML高亮
 *
 * @author CPoet
 */
public class XmlCodeAreaFactory extends CodeAreaFactory {

    private static final String XML_TAG_PATTERN = "(</?\\w+/?>|\\?>)";
    private static final String XML_ATTRIBUTE_PATTERN = "\\s(\\w+)\\s*=\\s*\"[^\"]*\"";
    private static final String XML_COMMENT_PATTERN = "<!--[\\s\\S]*?-->";
    private static final String XML_STRING_PATTERN = "\"[^\"]*\"";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<TAG>" + XML_TAG_PATTERN + ")"
                    + "|(?<ATTRIBUTE>" + XML_ATTRIBUTE_PATTERN + ")"
                    + "|(?<COMMENT>" + XML_COMMENT_PATTERN + ")"
                    + "|(?<STRING>" + XML_STRING_PATTERN + ")"
    );

    @Override
    public String getStyleSheetPath() {
        return "/css/xml-style.css";
    }

    @Override
    protected Matcher createMatcher(String text) {
        return PATTERN.matcher(text);
    }

    @Override
    protected String getStyleClass(Matcher matcher) {
        String styleClass = null;
        if (matcher.group("TAG") != null) {
            styleClass = "xml-tag";
        } else if (matcher.group("ATTRIBUTE") != null) {
            styleClass = "xml-attribute";
        } else if (matcher.group("COMMENT") != null) {
            styleClass = "xml-comment";
        } else if (matcher.group("STRING") != null) {
            styleClass = "xml-string";
        }
        return styleClass;
    }
}
