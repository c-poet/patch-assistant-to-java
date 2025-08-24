package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.exception.AppException;

import java.io.InputStream;

/**
 * @author CPoet
 */
public abstract class FileDecompressor {

    /**
     * 解压缩
     *
     * @param in       输入流
     * @param callback 回调
     */
    public abstract void decompress(InputStream in, UnCallback callback);


    /**
     * 判断是否支持的压缩类型
     *
     * @param fileName 文件名
     * @return 是否压缩的文件类型
     */
    public static boolean isCompressFile(String fileName) {
        return doGetInstance(fileName) != null;
    }

    /**
     * 获取压缩实例
     *
     * @param fileName 文件名
     * @return 压缩实例
     */
    public static FileDecompressor getInstance(String fileName) {
        FileDecompressor fileDecompressor = doGetInstance(fileName);
        if (fileDecompressor == null) {
            throw new AppException("Unsupported compression type: " + fileName);
        }
        return fileDecompressor;
    }

    private static FileDecompressor doGetInstance(String fileName) {
        if (fileName.endsWith(FileExtConst.DOT_JAR) || fileName.endsWith(FileExtConst.DOT_ZIP)) {
            return ZipFileDecompressor.INSTANCE;
        }
        if (fileName.endsWith(FileExtConst.DOT_RAR)) {
            return RarFileDecompressor.INSTANCE;
        }
        return null;
    }
}
