package cn.cpoet.patch.assistant.util;

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
            throw new RuntimeException(e);
        }
    }
}
