package cn.cpoet.patch.assistant.model;

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
    private String path1;

    /**
     * 一级路径
     */
    private String path2;

    /**
     * 二级路径
     */
    private String path3;

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public String getPath1() {
        return path1;
    }

    public void setPath1(String fileName) {
        this.path1 = fileName;
    }

    public String getPath2() {
        return path2;
    }

    public void setPath2(String path2) {
        this.path2 = path2;
    }

    public String getPath3() {
        return path3;
    }

    public void setPath3(String path3) {
        this.path3 = path3;
    }
}
