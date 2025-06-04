package cn.cpoet.patch.assistant.util;

import com.github.difflib.UnifiedDiffUtils;

import java.util.Arrays;
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
     * 比较并返回可视文本
     *
     * @param original     原文本
     * @param revised      目标文本
     * @param originalName 原名称
     * @param revisedName  目标名称
     * @return 比较结果
     */
    public static String diff(String original, String revised, String originalName, String revisedName) {
        List<String> leftContentLines = Arrays.asList(original.split("\n|(\r\n)"));
        List<String> rightContentLines = Arrays.asList(revised.split("\n|(\r\n)"));
        List<String> diffs = UnifiedDiffUtils.generateOriginalAndDiff(leftContentLines, rightContentLines, originalName, revisedName);
        // Diff-Util BUG Process
        diffs.set(1, "+++ " + revisedName);
        return String.join("\n", diffs);
    }
}
