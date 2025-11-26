package cn.cpoet.patch.assistant.model;

/**
 * 删除信息
 *
 * @author CPoet
 */
public class DelInfo {
    /**
     * 文件路径
     */
    private String appPath;

    /**
     * md5值
     */
    private String appMd5;

    /**
     * 文件创建时间
     */
    private String appCreateTime;

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getAppMd5() {
        return appMd5;
    }

    public void setAppMd5(String appMd5) {
        this.appMd5 = appMd5;
    }

    public String getAppCreateTime() {
        return appCreateTime;
    }

    public void setAppCreateTime(String appCreateTime) {
        this.appCreateTime = appCreateTime;
    }
}
