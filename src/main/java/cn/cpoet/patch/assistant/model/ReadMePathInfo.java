package cn.cpoet.patch.assistant.model;

import cn.cpoet.patch.assistant.constant.ChangeTypeEnum;

/**
 * Readme填写的路径信息
 *
 * @author CPoet
 */
public class ReadMePathInfo {

    /**
     * 类型
     */
    private ChangeTypeEnum type;

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

    public ChangeTypeEnum getType() {
        return type;
    }

    public void setType(ChangeTypeEnum type) {
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
