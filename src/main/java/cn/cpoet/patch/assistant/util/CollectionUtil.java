package cn.cpoet.patch.assistant.util;

import java.util.Collection;

/**
 * @author CPoet
 */
public abstract class CollectionUtil {

    private CollectionUtil() {
    }

    /**
     * 判断集合是否为空
     *
     * @param collection 集合
     * @param <T>        集合类型
     * @return 是否为空
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否不为空
     *
     * @param collection 集合
     * @param <T>        集合类型
     * @return 是否不为空
     */
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }
}
