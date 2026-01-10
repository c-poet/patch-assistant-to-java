package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.constant.FocusTreeStatusConst;
import cn.cpoet.patch.assistant.constant.LoadStatusConst;
import cn.cpoet.patch.assistant.control.tree.AppTreeView;
import cn.cpoet.patch.assistant.control.tree.PatchTreeView;
import cn.cpoet.patch.assistant.control.tree.TotalInfo;
import cn.cpoet.patch.assistant.core.Configuration;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * 上下文
 *
 * @author CPoet
 */
public class HomeContext {

    /** 是否显示补丁信息 */
    protected final BooleanProperty showPatchInfo;
    /** 聚焦类型：1-聚焦应用树 2-聚焦补丁树 */
    protected final IntegerProperty focusTreeStatus;
    protected final TotalInfo totalInfo = new TotalInfo();
    protected final AppTreeView appTree = new AppTreeView();
    protected final PatchTreeView patchTree = new PatchTreeView();
    /** 加载状态 */
    protected final IntegerProperty loadStatus = new SimpleIntegerProperty(LoadStatusConst.NOT_LOAD);

    public HomeContext() {
        focusTreeStatus = new SimpleIntegerProperty(Configuration.getInstance().getFocusTreeStatus() == null ? FocusTreeStatusConst.ALL : Configuration.getInstance().getFocusTreeStatus());
        showPatchInfo = new SimpleBooleanProperty(Boolean.TRUE.equals(Configuration.getInstance().getShowPatchInfo()));
    }

    public TotalInfo getTotalInfo() {
        return totalInfo;
    }

    public AppTreeView getAppTree() {
        return appTree;
    }

    public PatchTreeView getPatchTree() {
        return patchTree;
    }

    public boolean isLoading() {
        return loadStatus.get() != LoadStatusConst.NOT_LOAD;
    }

    public void updateLoadStatus(boolean status, int flag) {
        if (status) {
            loadStatus.set(loadStatus.get() | flag);
        } else {
            loadStatus.set(loadStatus.get() ^ flag);
        }
    }
}
