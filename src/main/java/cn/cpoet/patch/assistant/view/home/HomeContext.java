package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.constant.FocusTreeStatusConst;
import cn.cpoet.patch.assistant.control.tree.AppTreeView;
import cn.cpoet.patch.assistant.control.tree.PatchTreeView;
import cn.cpoet.patch.assistant.control.tree.TotalInfo;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.StartUpInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.File;

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

    public HomeContext() {
        focusTreeStatus = new SimpleIntegerProperty(getInitFocusTreeStatus());
        showPatchInfo = new SimpleBooleanProperty(getInitShowPatchInfo());
    }

    private int getInitFocusTreeStatus() {
        return StartUpInfo.consume(sci -> {
            int status = Configuration.getInstance().getFocusTreeStatus() == null ? FocusTreeStatusConst.ALL
                    : Configuration.getInstance().getFocusTreeStatus();
            File appFile = sci.getAppFile();
            File patchFile = sci.getPatchFile();
            if (appFile != null && patchFile != null) {
                if (sci.isArgAppFile() || sci.isArgPatchFile()) {
                    status = FocusTreeStatusConst.ALL;
                }
            } else if (appFile == null && patchFile != null) {
                if (status == FocusTreeStatusConst.APP) {
                    status = FocusTreeStatusConst.ALL;
                } else if (sci.isArgPatchFile()) {
                    status = FocusTreeStatusConst.PATCH;
                }
            } else if (appFile != null) {
                if (status == FocusTreeStatusConst.PATCH) {
                    status = FocusTreeStatusConst.ALL;
                } else if (sci.isArgAppFile()) {
                    status = FocusTreeStatusConst.APP;
                }
            }
            return status;
        });
    }

    private boolean getInitShowPatchInfo() {
        if (focusTreeStatus.get() == FocusTreeStatusConst.APP) {
            return false;
        }
        return Boolean.TRUE.equals(Configuration.getInstance().getShowPatchInfo());
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
}
