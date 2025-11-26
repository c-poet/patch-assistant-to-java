package cn.cpoet.patch.assistant.core;

/**
 * 内容查看配置
 *
 * @author CPoet
 */
public class ContentConf {
    /**
     * 差异对比模式
     */
    private Boolean diffModel = Boolean.FALSE;

    public Boolean getDiffModel() {
        return diffModel;
    }

    public void setDiffModel(Boolean diffModel) {
        this.diffModel = diffModel;
    }
}
