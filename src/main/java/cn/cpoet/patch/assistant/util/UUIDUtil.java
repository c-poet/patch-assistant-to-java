package cn.cpoet.patch.assistant.util;

import java.util.UUID;

/**
 * UUID工具
 *
 * @author CPoet
 */
public abstract class UUIDUtil {
    private UUIDUtil() {
    }

    public static String random() {
        return UUID.randomUUID().toString();
    }

    public static String random32() {
        return random().replaceAll("-", "");
    }
}
