package cn.cpoet.patch.assistant.model;

import java.util.Date;

/**
 * 应用包
 *
 * @author CPoet
 */
public class ModInfo {
    /**
     * 应用路径
     */
    private String appPath;

    /**
     * 补丁路径
     */
    private String patchPath;

    /**
     * 应用md5值
     */
    private String appMd5;

    /**
     * 补丁md5值
     */
    private String patchMd5;

    /**
     * 应用文件创建时间
     */
    private String appCreateTime;

    /**
     * 补丁文件创建时间
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

    public String getAppMd5() {
        return appMd5;
    }

    public void setAppMd5(String appMd5) {
        this.appMd5 = appMd5;
    }

    public String getPatchMd5() {
        return patchMd5;
    }

    public void setPatchMd5(String patchMd5) {
        this.patchMd5 = patchMd5;
    }

    public String getAppCreateTime() {
        return appCreateTime;
    }

    public void setAppCreateTime(String appCreateTime) {
        this.appCreateTime = appCreateTime;
    }

    public String getPatchCreateTime() {
        return patchCreateTime;
    }

    public void setPatchCreateTime(String patchCreateTime) {
        this.patchCreateTime = patchCreateTime;
    }
}
