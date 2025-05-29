package cn.cpoet.patch.assistant.service;

/**
 * Readme填写的路径信息
 *
 * @author CPoet
 */
public class ReadMePathInfo {
    /**
     * 文件名或者路径
     */
    private String fileName;

    /**
     * 一级路径
     */
    private String firstPath;

    /**
     * 二级路径
     */
    private String secondPath;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
