package cn.cpoet.patch.assistant.constant;

/**
 * 主题枚举
 *
 * @author CPoet
 */
public enum ThemeEnum {
    LIGHT("light", "明亮主题"),
    DARK("dark", "暗夜主题");

    private final String code;
    private final String name;

    ThemeEnum(final String code, final String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ThemeEnum ofCode(String code) {
        for (ThemeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static ThemeEnum ofName(String name) {
        for (ThemeEnum value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
