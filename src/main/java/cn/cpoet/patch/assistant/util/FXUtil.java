package cn.cpoet.patch.assistant.util;

import java.util.function.Consumer;

/**
 * 布局处理工具
 *
 * @author CPoet
 */
public abstract class FXUtil {
    private FXUtil() {
    }

    /**
     * 前置调用
     *
     * @param t        对象
     * @param consumer 对象消费者
     * @param <T>      类型
     * @return 对象
     */
    public static <T> T pre(T t, Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }
}
