package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.util.DateUtil;
import javafx.application.Platform;
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

    protected TextArea textArea;
    protected volatile boolean end;
    protected ProgressBar progressBar;
    protected volatile boolean runLater;

    public boolean isRunLater() {
        return runLater;
    }

    public void setRunLater(boolean runLater) {
        this.runLater = runLater;
    }

    public boolean isEnd() {
        return end;
    }

    private void doStep(String msg) {
        if (textArea != null) {
            String time = DateUtil.curDateTime();
            textArea.appendText("\n" + time + " " + msg);
        }
    }

    private void doEnd() {
        end = true;
        if (progressBar != null) {
            progressBar.setProgress(1);
        }
    }

    public void step(String msg) {
        if (runLater) {
            Platform.runLater(() -> doStep(msg));
        } else {
            doStep(msg);
        }
    }

    private void doOverwrite(String msg) {
        String text = textArea.getText();
        int i = text.lastIndexOf("\n");
        if (i == -1) {
            textArea.setText("");
        } else {
            textArea.deleteText(i, text.length());
        }
        doStep(msg);
    }

    public void overwrite(String msg) {
        if (runLater) {
            Platform.runLater(() -> doOverwrite(msg));
        } else {
            doOverwrite(msg);
        }
    }

    public void end() {
        if (runLater) {
            Platform.runLater(this::doEnd);
        } else {
            doEnd();
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
