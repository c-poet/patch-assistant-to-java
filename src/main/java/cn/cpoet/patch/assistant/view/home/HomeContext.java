package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.constant.FocusTreeStatusConst;
import cn.cpoet.patch.assistant.control.tree.AppTreeView;
import cn.cpoet.patch.assistant.control.tree.PatchTreeView;
import cn.cpoet.patch.assistant.control.tree.TotalInfo;
import cn.cpoet.patch.assistant.core.Configuration;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * 上下文
 *
 * @author CPoet
 */
public class HomeContext {

    protected final TotalInfo totalInfo = new TotalInfo();
    protected final AppTreeView appTree = new AppTreeView();
    protected final PatchTreeView patchTree = new PatchTreeView();
    /**
     * 聚焦类型：1-聚焦应用树 2-聚焦补丁树
     */
    protected final IntegerProperty focusTreeStatus;

    public HomeContext() {
        int focusTreeStatusVal = Configuration.getInstance().getFocusTreeStatus() == null ? FocusTreeStatusConst.ALL
                : Configuration.getInstance().getFocusTreeStatus();
        focusTreeStatus = new SimpleIntegerProperty(focusTreeStatusVal);
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
