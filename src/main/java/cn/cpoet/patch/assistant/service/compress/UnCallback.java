package cn.cpoet.patch.assistant.service.compress;

import java.io.InputStream;

/**
 * 解压回调
 *
 * @author CPoet
 */
@FunctionalInterface
public interface UnCallback {
    /**
     * 回调方法
     *
     * @param entry 文件信息
     * @param in    文件输入流（目录时为Null值）
     */
    void invoke(Object entry, InputStream in);
}
