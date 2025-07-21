package cn.cpoet.patch.assistant.control;

import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author CPoet
 */
public class StringMappingConverter<T> extends StringConverter<T> {

    private final Function<T, String> func;
    private final Map<String, T> mapping = new HashMap<>();

    public StringMappingConverter(Function<T, String> func) {
        this.func = func;
    }

    @Override
    public String toString(T object) {
        String str = func.apply(object);
        if (str != null) {
            mapping.put(str, object);
        }
        return str;
    }

    @Override
    public T fromString(String str) {
        return mapping.get(str);
    }
}
