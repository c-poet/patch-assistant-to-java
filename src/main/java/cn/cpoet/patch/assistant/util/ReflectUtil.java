package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;

/**
 * @author CPoet
 */
public abstract class ReflectUtil {
    private ReflectUtil() {
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new AppException("实例化对象失败", e);
        }
    }
}
