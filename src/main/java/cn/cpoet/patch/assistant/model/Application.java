package cn.cpoet.patch.assistant.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * 应用信息
 *
 * @author CPoet
 */
@JacksonXmlRootElement
public class Application {

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用版本
     */
    private String version;

    /**
     * 组织
     */
    private String groupId;

    /**
     * 项目名称
     */
    private String artifactId;

    /**
     * 编译用户名称
     */
    private String userName;

    /**
     * 编译时间
     */
    private String buildTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }
}
