package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;

/**
 * 反射工具
 *
 * @author CPoet
 */
public abstract class ReflectUtil {
    private ReflectUtil() {
    }

    /**
     * 使用默认构造器构造实例
     *
     * @param clazz 类型
     * @param <T>   类型
     * @return 实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new AppException("实例化对象失败", e);
        }
    }
}
