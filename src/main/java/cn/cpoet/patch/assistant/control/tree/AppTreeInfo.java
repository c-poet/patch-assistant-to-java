package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.model.AppPackSign;
import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 应用信息
 *
 * @author CPoet
 */
public class AppTreeInfo extends TreeInfo {

    /**
     * 应用包签名信息
     */
    private AppPackSign appPackSign;

    /**
     * 补丁比较信息
     */
    private final StringProperty patchDiffInfo = new SimpleStringProperty();

    public AppPackSign getAppPackSign() {
        return appPackSign;
    }

    public void setAppPackSign(AppPackSign appPackSign) {
        this.appPackSign = appPackSign;
    }

    public String getPatchDiffInfo() {
        return patchDiffInfo.get();
    }

    public StringProperty patchDiffInfoProperty() {
        return patchDiffInfo;
    }

    public void setPatchDiffInfo(String patchDiffInfo) {
        this.patchDiffInfo.set(patchDiffInfo);
    }

    public void appendPatchDiffInfo(String patchDiffInfo) {
        String s = this.patchDiffInfo.get();
        if (StringUtil.isBlank(s)) {
            this.patchDiffInfo.set(patchDiffInfo);
        } else {
            this.patchDiffInfo.set(s + '\n' + patchDiffInfo);
        }
    }
}
