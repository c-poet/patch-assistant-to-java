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
     * 滚动联动
     */
    @JacksonXmlProperty
    private Boolean isScrollLinked;

    /**
     * Docker镜像模式
     */
    @JacksonXmlProperty
    private Boolean isDockerImage;

    Configuration() {
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

    public Boolean getIsScrollLinked() {
        return isScrollLinked;
    }

    public void setIsScrollLinked(Boolean isScrollLinked) {
        this.isScrollLinked = isScrollLinked;
    }

    public Boolean getIsDockerImage() {
        return isDockerImage;
    }

    public void setIsDockerImage(Boolean isDockerImage) {
        this.isDockerImage = isDockerImage;
    }
}
