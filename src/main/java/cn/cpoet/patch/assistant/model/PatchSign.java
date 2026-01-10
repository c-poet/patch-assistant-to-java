package cn.cpoet.patch.assistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 补丁签名
 *
 * @author CPoet
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PatchSign {
    /**
     * 补丁编码
     */
    private String code;

    /**
     * 补丁名称
     */
    private String name;

    /**
     * 补丁md5值
     */
    private String md5;

    /**
     * 补丁sha1值
     */
    private String sha1;

    /**
     * 补丁包所在路径
     */
    private String path;

    /**
     * 补丁readme文件内容
     */
    private String readme;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }
}
