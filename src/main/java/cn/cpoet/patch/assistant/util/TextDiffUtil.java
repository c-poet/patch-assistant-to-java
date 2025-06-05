package cn.cpoet.patch.assistant.util;

import com.github.difflib.UnifiedDiffUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 文本比较工具
 *
 * @author CPoet
 */
public abstract class TextDiffUtil {

    private TextDiffUtil() {
    }

    /**
     * 比较
     *
     * @param original     原文本
     * @param revised      目标文本
     * @param originalName 原名称
     * @param revisedName  目标名称
     * @return 比较结果
     */
    public static List<String> diff(String original, String revised, String originalName, String revisedName) {
        List<String> leftContentLines = Arrays.asList(original.split("\n|(\r\n)"));
        List<String> rightContentLines = Arrays.asList(revised.split("\n|(\r\n)"));
        List<String> diffs = UnifiedDiffUtils.generateOriginalAndDiff(leftContentLines, rightContentLines, originalName, revisedName);
        // Diff-Util BUG Process
        diffs.set(1, "+++ " + revisedName);
        return diffs;
    }

    /**
     * 比较并拼接返回字符串
     *
     * @param original     原文本
     * @param revised      目标文本
     * @param originalName 原名称
     * @param revisedName  目标名称
     * @return 比较结果
     */
    public static String diff2Str(String original, String revised, String originalName, String revisedName) {
        List<String> diffs = diff(original, revised, originalName, revisedName);
        return diffJoin(diffs);
    }

    /**
     * 拼接比较结果
     *
     * @param diffs 比较结果
     * @return 拼接结果
     */
    public static String diffJoin(Collection<String> diffs) {
        return String.join("\n", diffs);
    }
}
