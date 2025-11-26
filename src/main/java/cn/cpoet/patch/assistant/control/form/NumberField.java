package cn.cpoet.patch.assistant.control.form;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

/**
 * 数字输入
 *
 * @author CPoet
 */
public abstract class NumberField<T extends Number> extends TextField {

    public NumberField() {
        setTextFormatter(createFormatter());
    }

    public T getNumber() {
        return fromString(getText());
    }

    public void setNumber(T number) {
        setText(toString(number));
    }

    protected abstract String toString(T val);

    protected abstract T fromString(String text);

    protected NumberFormatter<T> createFormatter() {
        return new NumberFormatter<>(this);
    }

    protected boolean filterChange(String oldText, String newText) {
        return true;
    }

    protected static class NumberFormatter<T extends Number> extends TextFormatter<T> {

        public NumberFormatter(NumberField<T> field) {
            super(new StringConverter<>() {
                @Override
                public String toString(T t) {
                    return field.toString(t);
                }

                @Override
                public T fromString(String s) {
                    return field.fromString(s);
                }
            }, null, change -> field.filterChange(change.getControlText(), change.getControlNewText()) ? change : null);
        }
    }
}
