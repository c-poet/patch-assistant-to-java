package cn.cpoet.patch.assistant.control;

import cn.cpoet.patch.assistant.util.StringUtil;

import java.util.regex.Pattern;

/**
 * @author CPoet
 */
public class IntegerField extends NumberField<Integer> {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d*$");

    @Override
    protected String toString(Integer val) {
        return val == null ? null : val.toString();
    }

    @Override
    protected Integer fromString(String text) {
        return StringUtil.isBlank(text) ? null : Integer.parseInt(text);
    }

    @Override
    protected boolean filterChange(String oldText, String newText) {
        return INTEGER_PATTERN.matcher(newText).matches();
    }
}
