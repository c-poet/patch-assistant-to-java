package cn.cpoet.patch.assistant.view.progress;

import cn.cpoet.patch.assistant.util.DateUtil;
import cn.cpoet.patch.assistant.util.UIUtil;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * 执行进度上下文
 *
 * @author CPoet
 */
public class ProgressContext {

    protected Dialog<?> dialog;
    protected TextField textField;
    protected volatile boolean end;
    protected ProgressBar progressBar;

    public boolean isEnd() {
        return end;
    }

    public void step(String msg) {
        if (textField != null) {
            UIUtil.runUI(() -> textField.setText(DateUtil.curDateTime() + " " + msg));
        }
    }

    public void end(boolean isClose) {
        UIUtil.runUI(() -> {
            end = true;
            if (progressBar != null) {
                progressBar.setProgress(1);
            }
        });
        if (isClose && dialog != null) {
            UIUtil.runUI(() -> dialog.close());
        }
    }
}
