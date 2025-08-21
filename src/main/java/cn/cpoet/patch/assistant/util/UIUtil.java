package cn.cpoet.patch.assistant.util;

import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;

/**
 * UI工具
 *
 * @author CPoet
 */
public abstract class UIUtil {
    private UIUtil() {
    }

    /**
     * 在ui线程下运行的任务
     *
     * @param runnable 执行的内容
     */
    public static void runUI(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    /**
     * 在非ui线程下执行的任务
     *
     * @param runnable 执行的内容
     */
    public static void runNotUI(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            CompletableFuture.runAsync(runnable);
        } else {
            runnable.run();
        }
    }
}
