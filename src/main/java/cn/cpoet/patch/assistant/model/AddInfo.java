package cn.cpoet.patch.assistant.model;

/**
 * @author CPoet
 */
public class AddInfo {
    /**
     * 应用文件路径
     */
    private String appPath;

    /**
     * 补丁文件路径
     */
    private String patchPath;

    /**
     * 补丁文件md值
     */
    private String patchMd5;

    /**
     * 文件创建时间
     */
    private String patchCreateTime;

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getPatchPath() {
        return patchPath;
    }

    public void setPatchPath(String patchPath) {
        this.patchPath = patchPath;
    }

    public String getPatchMd5() {
        return patchMd5;
    }

    public void setPatchMd5(String patchMd5) {
        this.patchMd5 = patchMd5;
    }

    public String getPatchCreateTime() {
        return patchCreateTime;
    }

    public void setPatchCreateTime(String patchCreateTime) {
        this.patchCreateTime = patchCreateTime;
    }
}
