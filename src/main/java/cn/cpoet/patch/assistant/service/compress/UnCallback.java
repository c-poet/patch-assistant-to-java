package cn.cpoet.patch.assistant.service.compress;

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
     * @param bytes 数据（目录时为Null值）
     */
    void invoke(Object entry, byte[] bytes);
}
