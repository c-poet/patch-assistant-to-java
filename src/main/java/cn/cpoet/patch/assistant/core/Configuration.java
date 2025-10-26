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
     * 绑定信息窗口宽度
     */
    @JacksonXmlProperty
    private Double nodeMappedWidth = 720D;

    /**
     * 绑定信息窗口高度
     */
    @JacksonXmlProperty
    private Double nodeMappedHeight = 560D;

    /**
     * 补丁签名查看窗口宽度
     */
    @JacksonXmlProperty
    private Double patchSignWidth = 680D;

    /**
     * 补丁签名查看窗口高度
     */
    @JacksonXmlProperty
    private Double patchSighHeight = 280D;

    /**
     * 选中联动
     */
    @JacksonXmlProperty
    private Boolean isSelectedLinked;

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
     * 显示补丁信息
     */
    @JacksonXmlProperty
    private Boolean showPatchInfo;

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
     * 应用包最后保存的路径
     */
    @JacksonXmlProperty
    private String lastSavePackPath;

    /**
     * 聚焦状态
     */
    @JacksonXmlProperty
    private Integer focusTreeStatus;

    /**
     * 常规配置
     */
    private GeneraConf genera = new GeneraConf();

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

    public Double getNodeMappedWidth() {
        return nodeMappedWidth;
    }

    public void setNodeMappedWidth(Double nodeMappedWidth) {
        this.nodeMappedWidth = nodeMappedWidth;
    }

    public Double getNodeMappedHeight() {
        return nodeMappedHeight;
    }

    public void setNodeMappedHeight(Double nodeMappedHeight) {
        this.nodeMappedHeight = nodeMappedHeight;
    }

    public Double getPatchSignWidth() {
        return patchSignWidth;
    }

    public void setPatchSignWidth(Double patchSignWidth) {
        this.patchSignWidth = patchSignWidth;
    }

    public Double getPatchSighHeight() {
        return patchSighHeight;
    }

    public void setPatchSighHeight(Double patchSighHeight) {
        this.patchSighHeight = patchSighHeight;
    }

    public Boolean getIsSelectedLinked() {
        return isSelectedLinked;
    }

    public void setIsSelectedLinked(Boolean isSelectedLinked) {
        this.isSelectedLinked = isSelectedLinked;
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

    public Boolean getShowPatchInfo() {
        return showPatchInfo;
    }

    public void setShowPatchInfo(Boolean showPatchInfo) {
        this.showPatchInfo = showPatchInfo;
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

    public String getLastSavePackPath() {
        return lastSavePackPath;
    }

    public void setLastSavePackPath(String lastSavePackPath) {
        this.lastSavePackPath = lastSavePackPath;
    }

    public Integer getFocusTreeStatus() {
        return focusTreeStatus;
    }

    public void setFocusTreeStatus(Integer focusTreeStatus) {
        this.focusTreeStatus = focusTreeStatus;
    }

    public GeneraConf getGenera() {
        return genera;
    }

    public void setGenera(GeneraConf genera) {
        this.genera = genera;
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
