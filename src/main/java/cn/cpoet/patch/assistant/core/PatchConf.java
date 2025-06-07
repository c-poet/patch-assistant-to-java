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

    public String getReadmeFile() {
        return readmeFile;
    }

    public void setReadmeFile(String readmeFile) {
        this.readmeFile = readmeFile;
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
