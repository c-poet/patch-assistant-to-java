package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.constant.I18NEnum;
import cn.cpoet.patch.assistant.core.Configuration;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * 国际化工具
 *
 * @author CPoet
 */
public abstract class I18nUtil {

    private static final String I18N_FILE_PREFIX = "messages/i18n";

    /** I18n资源 */
    private static ResourceBundle resourceBundle;

    static {
        initLocale();
    }

    private I18nUtil() {
    }

    /**
     * 获取国际化内容
     *
     * @param name 名称
     * @return 值
     */
    public static String t(String name) {
        return t(name, "");
    }

    /**
     * 获取国际化内容
     *
     * @param name           名称
     * @param defaultMessage 默认值
     * @return 值
     */
    public static String t(String name, String defaultMessage) {
        try {
            return resourceBundle.getString(name);
        } catch (Exception ignored) {
        }
        return defaultMessage;
    }

    /**
     * 获取国际化内容
     *
     * @param name   名称
     * @param params 参数列表
     * @return 值
     */
    public static String tr(String name, Object... params) {
        String message = t(name);
        return message == null || message.isBlank() ? message : String.format(message, params);
    }

    public static I18NEnum getLanguage() {
        String language = Configuration.getInstance().getGenera().getLanguage();
        return I18NEnum.ofCode(language);
    }

    private static void initLocale() {
        updateLocale();
    }

    public static void updateLocale() {
        Locale locale = getLanguage().toLocale();
        if (resourceBundle != null && Objects.equals(resourceBundle.getLocale(), locale)) {
            return;
        }
        resourceBundle = ResourceBundle.getBundle(I18N_FILE_PREFIX, locale);
    }
}
