package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.exception.AppException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * 异常工具
 *
 * @author CPoet
 */
public abstract class ExceptionUtil {
    private ExceptionUtil() {
    }

    /**
     * 转换为字符串
     *
     * @param e 异常
     * @return 字符串
     */
    public static String asString(Throwable e) {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            e.printStackTrace(printWriter);
            printWriter.flush();
            return stringWriter.toString();
        } catch (IOException ex) {
            throw new AppException("Converting an exception stack to a string failed", e);
        }
    }

    /**
     * 捕获异常并输出到文件中
     *
     * @param runnable 运行的方法
     */
    public static void runAsTryCatch(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            writeErrorFile(Thread.currentThread(), e);
        }
    }

    public static void writeErrorFile(Thread thread, Throwable e) {
        writeErrorFile(thread, asString(e));
    }

    public static void writeErrorFile(Thread thread, String content) {
        String message = String.format("%s %s %s", DateUtil.curDateTime(), thread.getName(), content);
        FileUtil.writeFile(OSUtil.getAppConfigPath().resolve(AppConst.ERROR_LOG_FILE).toFile(), message.getBytes(StandardCharsets.UTF_8));
        if (AppContext.getInstance().isDev()) {
            System.err.println(message);
        }
    }
}
