package cn.cpoet.patch.assistant.view;

import java.util.regex.Pattern;

/**
 * 搜索关键字
 *
 * @author CPoet
 */
public class SearchKeyword {
    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 匹配正则
     */
    private Pattern pattern;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
