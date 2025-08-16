package cn.cpoet.patch.assistant.view.content.facotry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CPoet
 */
public class YamlCodeAreaFactory extends CodeAreaFactory {

    private static final String[] YAML_KEYWORDS = new String[]{
            "true", "false", "null", "yes", "no", "on", "off"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", YAML_KEYWORDS) + ")\\b";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
    private static final String COMMENT_PATTERN = "#[^\n]*";
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";
    private static final String KEY_PATTERN = "^\\s*[a-zA-Z_][a-zA-Z0-9_]*:";
    private static final String ANCHOR_PATTERN = "&[a-zA-Z_][a-zA-Z0-9_]*";
    private static final String ALIAS_PATTERN = "\\*[a-zA-Z_][a-zA-Z0-9_]*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<KEY>" + KEY_PATTERN + ")"
                    + "|(?<ANCHOR>" + ANCHOR_PATTERN + ")"
                    + "|(?<ALIAS>" + ALIAS_PATTERN + ")"
    );

    @Override
    public String getStyleSheetPath() {
        return "/css/yaml-style.css";
    }

    @Override
    protected Matcher createMatcher(String text) {
        return PATTERN.matcher(text);
    }

    @Override
    protected String getStyleClass(Matcher matcher) {
        String styleClass = null;
        if (matcher.group("KEYWORD") != null) {
            styleClass = "yaml-keyword";
        } else if (matcher.group("STRING") != null) {
            styleClass = "yaml-string";
        } else if (matcher.group("COMMENT") != null) {
            styleClass = "yaml-comment";
        } else if (matcher.group("NUMBER") != null) {
            styleClass = "yaml-number";
        } else if (matcher.group("KEY") != null) {
            styleClass = "yaml-key";
        } else if (matcher.group("ANCHOR") != null) {
            styleClass = "yaml-anchor";
        } else if (matcher.group("ALIAS") != null) {
            styleClass = "yaml-alias";
        }
        return styleClass;
    }
}
