package cn.cpoet.patch.assistant.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单模板工具
 *
 * @author CPoet
 */
public abstract class TemplateUtil {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)}");

    private TemplateUtil() {
    }

    /**
     * 渲染
     *
     * @param template 模板
     * @param vars     变量表
     * @return 渲染结果
     */
    public static String render(String template, Map<String, Object> vars) {
        Matcher matcher = VAR_PATTERN.matcher(template);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            do {
                Object obj = vars.get(matcher.group(1));
                if (obj == null) {
                    sb.append(template, i, matcher.end(1) + 1);
                } else {
                    sb.append(template, i, matcher.start(1) - 2);
                    sb.append(obj);
                }
                i = matcher.end(1) + 1;
            } while (matcher.find() && i < template.length());
            if (i < template.length()) {
                sb.append(template, i, template.length());
            }
            return sb.toString();
        }
        return template;
    }
}
