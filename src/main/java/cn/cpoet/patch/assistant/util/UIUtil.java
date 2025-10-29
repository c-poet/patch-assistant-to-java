package cn.cpoet.patch.assistant.util;

import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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

    /**
     * 延迟执行
     *
     * @param times    秒数
     * @param runnable 执行的内容
     */
    public static void timeout(long times, TimeUnit unit, Runnable runnable) {
        long millis = unit.toMillis(times);
        long startMillis = System.currentTimeMillis();
        runNotUI(() -> {
            long waitMillis;
            while ((waitMillis = System.currentTimeMillis() - startMillis) < millis) {
                try {
                    TimeUnit.MILLISECONDS.sleep(waitMillis);
                } catch (Exception ignored) {
                }
            }
            runnable.run();
        });
    }
}
