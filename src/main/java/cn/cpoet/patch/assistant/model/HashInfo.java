package cn.cpoet.patch.assistant.model;

/**
 * 哈希信息
 *
 * @author CPoet
 */
public class HashInfo {

    /**
     * 长度
     */
    private int length;

    /**
     * md5串
     */
    private String md5;

    /**
     * sha1串
     */
    private String sha1;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

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
