package cn.cpoet.patch.assistant.util;

/**
 * 字符串工具
 *
 * @author CPoet
 */
public abstract class StringUtil {

    private StringUtil() {
    }

    /**
     * 判断字符串是否为空
     *
     * @param charSequence 字符序列
     * @return 字符串是否为空
     */
    public static boolean isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * 判断字符串是否为空白串
     *
     * @param charSequence 字符序列
     * @return 是否为空白串
     */
    public static boolean isBlank(CharSequence charSequence) {
        return isEmpty(charSequence) || charSequence.chars().allMatch(Character::isSpaceChar);
    }
}
