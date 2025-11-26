package cn.cpoet.patch.assistant.control;

import javafx.scene.Node;
import javafx.scene.control.DialogPane;

/**
 * {@link  javafx.scene.control.Dialog}面板，隐藏底部按钮区
 *
 * @author CPoet
 */
public class DialogPurePane extends DialogPane {
    @Override
    protected Node createButtonBar() {
        return null;
    }
}
