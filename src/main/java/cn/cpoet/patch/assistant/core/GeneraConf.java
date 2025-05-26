package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.I18NEnum;

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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
