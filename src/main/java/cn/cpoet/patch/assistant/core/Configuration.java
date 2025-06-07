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
     * 配置窗口宽度
     */
    @JacksonXmlProperty
    private Double configWidth = 720D;

    /**
     * 配置窗口高度
     */
    @JacksonXmlProperty
    private Double configHeight = 560D;

    /**
     * 搜索窗口宽度
     */
    @JacksonXmlProperty
    private Double searchWidth = 720D;

    /**
     * 搜索窗口高度
     */
    @JacksonXmlProperty
    private Double searchHeight = 300D;

    /**
     * 内容查看窗口宽度
     */
    @JacksonXmlProperty
    private Double contentWidth;

    /**
     * 内容查询窗口高度
     */
    @JacksonXmlProperty
    private Double contentHeight;

    /**
     * 进度窗口宽度
     */
    @JacksonXmlProperty
    private Double progressWidth = 720D;

    /**
     * 进度窗口高度
     */
    @JacksonXmlProperty
    private Double progressHeight = 560D;

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

    /**
     * 搜索配置
     */
    private SearchConf search = new SearchConf();

    /**
     * 补丁配置
     */
    private PatchConf patch = new PatchConf();

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

    public Double getConfigWidth() {
        return configWidth;
    }

    public void setConfigWidth(Double configWidth) {
        this.configWidth = configWidth;
    }

    public Double getConfigHeight() {
        return configHeight;
    }

    public void setConfigHeight(Double configHeight) {
        this.configHeight = configHeight;
    }

    public Double getSearchWidth() {
        return searchWidth;
    }

    public void setSearchWidth(Double searchWidth) {
        this.searchWidth = searchWidth;
    }

    public Double getSearchHeight() {
        return searchHeight;
    }

    public void setSearchHeight(Double searchHeight) {
        this.searchHeight = searchHeight;
    }

    public Double getContentWidth() {
        return contentWidth;
    }

    public void setContentWidth(Double contentWidth) {
        this.contentWidth = contentWidth;
    }

    public Double getContentHeight() {
        return contentHeight;
    }

    public void setContentHeight(Double contentHeight) {
        this.contentHeight = contentHeight;
    }

    public Double getProgressWidth() {
        return progressWidth;
    }

    public void setProgressWidth(Double progressWidth) {
        this.progressWidth = progressWidth;
    }

    public Double getProgressHeight() {
        return progressHeight;
    }

    public void setProgressHeight(Double progressHeight) {
        this.progressHeight = progressHeight;
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

    public SearchConf getSearch() {
        return search;
    }

    public void setSearch(SearchConf search) {
        this.search = search;
    }

    public PatchConf getPatch() {
        return patch;
    }

    public void setPatch(PatchConf patch) {
        this.patch = patch;
    }
}
