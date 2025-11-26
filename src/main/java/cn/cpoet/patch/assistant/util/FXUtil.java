package cn.cpoet.patch.assistant.util;

import javafx.scene.Node;

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
     * @param node   对象
     * @param <T>    类型
     * @param styles 指定样式列表
     * @return 对象
     */
    public static <T extends Node> T pre(T node, String... styles) {
        return pre(node, null, styles);
    }

    /**
     * 前置调用
     *
     * @param node     对象
     * @param consumer 对象消费者
     * @param styles   指定样式列表
     * @param <T>      类型
     * @return 对象
     */
    public static <T extends Node> T pre(T node, Consumer<T> consumer, String... styles) {
        if (consumer != null) {
            consumer.accept(node);
        }
        if (styles.length != 0) {
            node.setStyle(String.join(";", styles));
        }
        return node;
    }
}
