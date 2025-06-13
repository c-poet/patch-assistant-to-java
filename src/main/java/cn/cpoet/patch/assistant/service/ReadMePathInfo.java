package cn.cpoet.patch.assistant.service;

/**
 * Readme填写的路径信息
 *
 * @author CPoet
 */
public class ReadMePathInfo {

    public enum TypeEnum {
        NONE(""),
        ADD("+"),
        MOD("!"),
        DEL("-");

        private final String code;

        TypeEnum(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static TypeEnum ofCode(String code) {
            for (TypeEnum value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return TypeEnum.NONE;
        }
    }

    /**
     * 类型
     */
    private TypeEnum type;

    /**
     * 文件名或者路径
     */
    private String filePath;

    /**
     * 一级路径
     */
    private String firstPath;

    /**
     * 二级路径
     */
    private String secondPath;

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String fileName) {
        this.filePath = fileName;
    }

    public String getFirstPath() {
        return firstPath;
    }

    public void setFirstPath(String firstPath) {
        this.firstPath = firstPath;
    }

    public String getSecondPath() {
        return secondPath;
    }

    public void setSecondPath(String secondPath) {
        this.secondPath = secondPath;
    }
}
