package cn.cpoet.patch.assistant.util;

import org.mozilla.universalchardet.UniversalDetector;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符集工具
 *
 * @author CPoet
 */
public abstract class CharsetUtil {
    private CharsetUtil() {
    }

    public static Charset getCharset(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector();
        detector.handleData(bytes);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        return encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8;
    }
}
