package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.constant.AppConst;
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
            writeErrorFile(e);
        }
    }

    public static void writeErrorFile(Throwable e) {
        writeErrorFile(asString(e));
    }

    public static void writeErrorFile(String content) {
        FileUtil.writeFile(OSUtil.getExecDir(), AppConst.ERROR_LOG_FILE, content.getBytes(StandardCharsets.UTF_8));
    }
}
