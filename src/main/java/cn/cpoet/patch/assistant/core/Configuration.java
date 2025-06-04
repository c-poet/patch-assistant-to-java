package cn.cpoet.patch.assistant.core;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * 应用配置
 *
 * @author CPoet
 */
@JacksonXmlRootElement
public class Configuration {
    /**
     * 主界面宽度
     */
    @JacksonXmlProperty
    private Double homeWidth = 720D;

    /**
     * 主界面高度
     */
    @JacksonXmlProperty
    private Double homeHeight = 600D;

    /**
     * 选中联动
     */
    @JacksonXmlProperty
    private Boolean isSelectedLinked;

    /**
     * Docker镜像模式
     */
    @JacksonXmlProperty
    private Boolean isDockerImage;

    /**
     * 仅看变化的文件
     */
    @JacksonXmlProperty
    private Boolean isOnlyChanges;

    /**
     * 显示文件的详细信息
     */
    @JacksonXmlProperty
    private Boolean isShowFileDetail;

    /**
     * 最后的应用包路径
     */
    @JacksonXmlProperty
    private String lastAppPackPath;

    /**
     * 最后的补丁包路径
     */
    @JacksonXmlProperty
    private String lastPatchPackPath;

    /**
     * 常规配置
     */
    private GeneraConf genera = new GeneraConf();

    /**
     * Docker配置
     */
    private DockerConf docker = new DockerConf();

    /**
     * 内容查看页面配置
     */
    private ContentConf content = new ContentConf();

    Configuration() {
    }

    public static Configuration getInstance() {
        return AppContext.getInstance().getConfiguration();
    }

    public Double getHomeWidth() {
        return homeWidth;
    }

    public void setHomeWidth(Double homeWidth) {
        this.homeWidth = homeWidth;
    }

    public Double getHomeHeight() {
        return homeHeight;
    }

    public void setHomeHeight(Double homeHeight) {
        this.homeHeight = homeHeight;
    }

    public Boolean getIsSelectedLinked() {
        return isSelectedLinked;
    }

    public void setIsSelectedLinked(Boolean isSelectedLinked) {
        this.isSelectedLinked = isSelectedLinked;
    }

    public Boolean getIsDockerImage() {
        return isDockerImage;
    }

    public void setIsDockerImage(Boolean isDockerImage) {
        this.isDockerImage = isDockerImage;
    }

    public Boolean getIsOnlyChanges() {
        return isOnlyChanges;
    }

    public void setIsOnlyChanges(Boolean isOnlyChanges) {
        this.isOnlyChanges = isOnlyChanges;
    }

    public Boolean getIsShowFileDetail() {
        return isShowFileDetail;
    }

    public void setIsShowFileDetail(Boolean isShowFileDetail) {
        this.isShowFileDetail = isShowFileDetail;
    }

    public String getLastAppPackPath() {
        return lastAppPackPath;
    }

    public void setLastAppPackPath(String lastAppPackPath) {
        this.lastAppPackPath = lastAppPackPath;
    }

    public String getLastPatchPackPath() {
        return lastPatchPackPath;
    }

    public void setLastPatchPackPath(String lastPatchPackPath) {
        this.lastPatchPackPath = lastPatchPackPath;
    }

    public GeneraConf getGenera() {
        return genera;
    }

    public void setGenera(GeneraConf genera) {
        this.genera = genera;
    }

    public DockerConf getDocker() {
        return docker;
    }

    public void setDocker(DockerConf docker) {
        this.docker = docker;
    }

    public ContentConf getContent() {
        return content;
    }

    public void setContent(ContentConf content) {
        this.content = content;
    }
}
