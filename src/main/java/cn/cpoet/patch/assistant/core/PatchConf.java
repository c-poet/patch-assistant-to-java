package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;

/**
 * 补丁配置
 *
 * @author CPoet
 */
public class PatchConf implements Cloneable {

    /**
     * 说明文件名称
     */
    private String readmeFile = AppConst.README_FILE;

    /**
     * 按照路径匹配
     */
    private Boolean pathMatch = Boolean.TRUE;

    /**
     * 按照文件名匹配
     */
    private Boolean fileNameMatch = Boolean.FALSE;

    /**
     * 写入补丁元信息
     */
    private Boolean writePatchMeta = Boolean.TRUE;

    public String getReadmeFile() {
        return readmeFile;
    }

    public void setReadmeFile(String readmeFile) {
        this.readmeFile = readmeFile;
    }

    public Boolean getPathMatch() {
        return pathMatch;
    }

    public void setPathMatch(Boolean pathMatch) {
        this.pathMatch = pathMatch;
    }

    public Boolean getFileNameMatch() {
        return fileNameMatch;
    }

    public void setFileNameMatch(Boolean fileNameMatch) {
        this.fileNameMatch = fileNameMatch;
    }

    public Boolean getWritePatchMeta() {
        return writePatchMeta;
    }

    public void setWritePatchMeta(Boolean writePatchMeta) {
        this.writePatchMeta = writePatchMeta;
    }

    @Override
    public PatchConf clone() {
        try {
            return (PatchConf) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
