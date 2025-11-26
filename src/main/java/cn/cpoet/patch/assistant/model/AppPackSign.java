package cn.cpoet.patch.assistant.model;

/**
 * 应用包信息
 *
 * @author CPoet
 */
public class AppPackSign {
    /**
     * 补丁md5值
     */
    private String md5;

    /**
     * 补丁sha1值
     */
    private String sha1;

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
}
