package cn.cpoet.patch.assistant.constant;

/**
 * Patch change type enum
 *
 * @author CPoet
 */
public enum ChangeTypeEnum {
    /**
     * None，Automatically judge based on the application package
     */
    NONE(""),
    /**
     * Add
     */
    ADD("+"),
    /**
     * Modify
     */
    MOD("!"),
    /**
     * Delete
     */
    DEL("-"),
    /**
     * Ignore this patch file
     */
    IGNORE("?"),
    ;

    private final String code;

    ChangeTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ChangeTypeEnum ofCode(String code) {
        for (ChangeTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return ChangeTypeEnum.NONE;
    }
}
