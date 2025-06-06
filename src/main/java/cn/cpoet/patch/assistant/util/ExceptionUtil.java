package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
            throw new AppException("转换异常堆栈为字符串失败", e);
        }
    }
}
