package cn.cpoet.patch.assistant.model;

/**
 * 应用包信息
 *
 * @author CPoet
 */
public class AppPackSign {
    /**
     * 应用包md5值
     */
    private String md5;

    /**
     * 应用包sha1值
     */
    private String sha1;

    /**
     * 应用包所在路径
     */
    private String path;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
