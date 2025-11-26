package cn.cpoet.patch.assistant.view.content.facotry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CPoet
 */
public class SQLCodeAreaFactory extends CodeAreaFactory {

    private static final String[] SQL_KEYWORDS = new String[]{
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "IN", "IS", "NULL",
            "LIKE", "BETWEEN", "JOIN", "INNER", "OUTER", "LEFT", "RIGHT", "FULL",
            "ON", "GROUP BY", "HAVING", "ORDER BY", "ASC", "DESC", "INSERT", "INTO",
            "VALUES", "UPDATE", "SET", "DELETE", "CREATE", "ALTER", "DROP", "TABLE",
            "VIEW", "INDEX", "TRIGGER", "PROCEDURE", "FUNCTION", "DATABASE", "GRANT",
            "REVOKE", "COMMIT", "ROLLBACK", "BEGIN", "TRANSACTION", "CASE", "WHEN",
            "THEN", "ELSE", "END", "DISTINCT", "UNION", "ALL", "EXISTS", "WITH"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", SQL_KEYWORDS) + ")\\b";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
    private static final String NUMBER_PATTERN = "\\b\\d+\\b";
    private static final String COMMENT_PATTERN = "--[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    @Override
    public String getStyleSheetPath() {
        return "/css/sql-style.css";
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
        } else if (matcher.group("STRING") != null) {
            styleClass = "string";
        } else if (matcher.group("NUMBER") != null) {
            styleClass = "number";
        } else if (matcher.group("COMMENT") != null) {
            styleClass = "comment";
        }
        return styleClass;
    }
}
