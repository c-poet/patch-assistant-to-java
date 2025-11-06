package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.util.EnvUtil;
import cn.cpoet.patch.assistant.util.StringUtil;

/**
 * 补丁配置
 *
 * @author CPoet
 */
public class PatchConf implements Cloneable {

    /**
     * 当前补丁操作人
     */
    private String username;

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
    private Boolean fileNameMatch = Boolean.TRUE;

    /**
     * 按照名称或者路径匹配时，允许的文件
     */
    private String patchFileRegex = "(.*\\.class)|(.*\\.html)|(.*\\.js)|(.*\\.css)";

    /**
     * 写入补丁签名
     */
    private Boolean writePatchSign = Boolean.TRUE;

    /**
     * 补丁文件比较
     */
    private Boolean patchFileDiff = Boolean.TRUE;

    /**
     * 打开应用时加载最后操作的补丁
     */
    private Boolean loadLastPatch = Boolean.TRUE;

    public String getUsername() {
        return StringUtil.isBlank(username) ? EnvUtil.getUserName() : username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public String getPatchFileRegex() {
        return patchFileRegex;
    }

    public void setPatchFileRegex(String patchFileRegex) {
        this.patchFileRegex = patchFileRegex;
    }

    public Boolean getWritePatchSign() {
        return writePatchSign;
    }

    public void setWritePatchSign(Boolean writePatchSign) {
        this.writePatchSign = writePatchSign;
    }

    public Boolean getPatchFileDiff() {
        return patchFileDiff;
    }

    public void setPatchFileDiff(Boolean patchFileDiff) {
        this.patchFileDiff = patchFileDiff;
    }

    public Boolean getLoadLastPatch() {
        return loadLastPatch;
    }

    public void setLoadLastPatch(Boolean loadLastPatch) {
        this.loadLastPatch = loadLastPatch;
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
