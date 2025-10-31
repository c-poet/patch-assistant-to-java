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
        char c;
        int i = 0, j = 0;
        char[] chars = new char[32];
        String random = random();
        while (i < random.length()) {
            c = random.charAt(i);
            if (c != '-') {
                chars[j++] = c;
            }
            ++i;
        }
        return new String(chars);
    }
}
