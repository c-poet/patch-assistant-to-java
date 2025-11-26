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

    /**
     * 获取指定字符在序列最后的下标并返回总数
     *
     * @param charSequence 序列
     * @param c            字符
     * @return 0-下标，1-总数
     */
    public static int[] lastIndexOfAndCount(CharSequence charSequence, char c) {
        int[] ret = new int[]{-1, 0};
        for (int i = 0; i < charSequence.length(); ++i) {
            if (charSequence.charAt(i) == c) {
                ret[0] = i;
                ++ret[1];
            }
        }
        return ret;
    }
}
