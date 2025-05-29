package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

/**
 * 文件缓存工具
 *
 * @author CPoet
 */
public abstract class FileTempUtil {

    private FileTempUtil() {
    }

    public static File createTempFile(String fileName, FileAttribute<?>... attrs) {
        String name = FileNameUtil.getName(fileName);
        String ext = FileNameUtil.getExt(fileName);
        return createTempFile(name, ext, attrs);
    }

    public static File createTempFile(String fileName, byte[] bytes, FileAttribute<?>... attrs) {
        File file = createTempFile(fileName, attrs);
        FileUtil.writeFile(file, bytes);
        return file;
    }

    public static File createTempFile(String fileName, String ext, FileAttribute<?>... attrs) {
        try {
            if (ext != null && !ext.isEmpty() && ext.charAt(0) != FileNameUtil.C_EXT_SEPARATOR) {
                ext = FileNameUtil.C_EXT_SEPARATOR + ext;
            }
            return Files.createTempFile(fileName, ext, attrs).toFile();
        } catch (Exception e) {
            throw new AppException("创建缓存文件失败", e);
        }
    }

    public static void deleteTempFile(File file) {
        if (!FileUtil.deleteFile(file) && file.exists()) {
            throw new AppException("删除缓存文件失败");
        }
    }
}
