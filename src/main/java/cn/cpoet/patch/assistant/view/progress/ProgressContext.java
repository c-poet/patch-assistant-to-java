package cn.cpoet.patch.assistant.view.progress;

import cn.cpoet.patch.assistant.util.DateUtil;
import cn.cpoet.patch.assistant.util.UIUtil;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * 执行进度上下文
 *
 * @author CPoet
 */
public class ProgressContext {

    protected Dialog<?> dialog;
    protected TextArea textArea;
    protected volatile boolean end;
    protected ProgressBar progressBar;

    public boolean isEnd() {
        return end;
    }

    public void step(String msg) {
        if (textArea != null) {
            String time = DateUtil.curDateTime();
            UIUtil.runUI(() -> textArea.appendText("\n" + time + " " + msg));
        }
    }

    public void overwrite(String msg) {
        UIUtil.runUI(() -> {
            String text = textArea.getText();
            int i = text.lastIndexOf("\n");
            if (i == -1) {
                textArea.setText("");
            } else {
                textArea.deleteText(i, text.length());
            }
            step(msg);
        });
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

    public OutputStream createOutputStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void flush() {
                if (count > 0) {
                    ProgressContext.this.step(toString());
                }
                reset();
            }

            @Override
            public void close() {
                flush();
            }
        };
    }
}
