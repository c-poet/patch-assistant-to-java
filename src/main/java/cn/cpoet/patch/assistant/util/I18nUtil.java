package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.constant.I18NEnum;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.exception.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * 国际化工具
 *
 * @author CPoet
 */
public abstract class I18nUtil {

    private static final String I18N_FILE_PREFIX = "/messages/i18n";
    private static final String I18N_FILE_EXT = ".properties";

    /**
     * 当前Locale
     */
    private static I18NEnum i18NEnum;
    /**
     * I18n资源
     */
    private static Properties i18nProperties;

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
            return i18nProperties.getProperty(name);
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

    /**
     * 获取系统语言
     *
     * @return 系统语言
     */
    public static I18NEnum getSysLanguage() {
        String language = Locale.getDefault().getLanguage();
        if ("en".equals(language)) {
            return I18NEnum.EN_US;
        }
        return I18NEnum.ZH_CN;
    }

    /**
     * 获取配置语言
     *
     * @return 配置语言
     */
    public static I18NEnum getLanguage() {
        String language = Configuration.getInstance().getGenera().getLanguage();
        return I18NEnum.ofCode(language);
    }

    private static void initLocale() {
        updateLocale();
    }

    public synchronized static void updateLocale() {
        I18NEnum language = getLanguage();
        if (i18nProperties != null && Objects.equals(I18nUtil.i18NEnum, language)) {
            return;
        }
        if (i18nProperties == null) {
            i18nProperties = new Properties();
        } else {
            i18nProperties.clear();
        }
        try (InputStream in = FileUtil.getFileAsStream(I18N_FILE_PREFIX + "_" + language.getCode() + I18N_FILE_EXT);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            i18nProperties.load(reader);
            I18nUtil.i18NEnum = language;
        } catch (IOException e) {
            throw new AppException("Load I18n failed", e);
        }
    }
}
