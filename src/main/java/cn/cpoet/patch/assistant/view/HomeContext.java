package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.view.tree.AppTreeView;
import cn.cpoet.patch.assistant.view.tree.PatchTreeView;
import cn.cpoet.patch.assistant.view.tree.TotalInfo;

/**
 * 上下文
 *
 * @author CPoet
 */
public class HomeContext {

    protected final TotalInfo totalInfo = new TotalInfo();
    protected final AppTreeView appTree = new AppTreeView();
    protected final PatchTreeView patchTree = new PatchTreeView();

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
