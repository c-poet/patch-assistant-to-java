package cn.cpoet.patch.assistant.constant;

import java.util.Objects;

/**
 * 支持的语言
 *
 * @author CPoet
 */
public enum I18NEnum {
    EN_US("en-US", "英文(默认)"),

    ZH_CN("zh-CN", "简体中文");

    private final String code;
    private final String name;

    I18NEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static I18NEnum ofCode(String code) {
        for (I18NEnum value : I18NEnum.values()) {
            if (Objects.equals(code, value.code)) {
                return value;
            }
        }
        return EN_US;
    }

    public static I18NEnum ofName(String name) {
        for (I18NEnum value : values()) {
            if (Objects.equals(name, value.name)) {
                return value;
            }
        }
        return null;
    }
}
