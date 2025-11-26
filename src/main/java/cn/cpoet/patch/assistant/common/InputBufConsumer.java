package cn.cpoet.patch.assistant.common;

import java.io.IOException;

/**
 * 输入流读取消费
 *
 * @author CPoet
 */
@FunctionalInterface
public interface InputBufConsumer {
    /**
     * @param len 当前读取的长度
     * @param buf 缓冲内容
     */
    void accept(int len, byte[] buf) throws IOException;
}
