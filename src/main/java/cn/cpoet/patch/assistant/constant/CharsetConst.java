package cn.cpoet.patch.assistant.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 常用字符集
 *
 * @author CPoet
 */
public interface CharsetConst {
    /**
     * UTF-8
     */
    Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * GBK
     */
    Charset GBK = Charset.forName("GBK");
}
