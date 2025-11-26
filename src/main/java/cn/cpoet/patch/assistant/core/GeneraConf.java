package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.I18NEnum;
import cn.cpoet.patch.assistant.constant.ThemeEnum;

/**
 * 普通配置
 *
 * @author CPoet
 */
public class GeneraConf implements Cloneable {

    /**
     * 当前使用的语言
     */
    private String language = I18NEnum.EN_US.getCode();

    /**
     * 当前使用的主题
     */
    private String theme = ThemeEnum.LIGHT.getCode();

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public GeneraConf clone() {
        try {
            return (GeneraConf) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
