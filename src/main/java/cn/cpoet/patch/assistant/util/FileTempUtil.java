package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * 文件缓存工具
 *
 * @author CPoet
 */
public abstract class FileTempUtil {

    public static final String TEMP_FILE_EXT = FileNameUtil.EXT_SEPARATOR + "temp";

    private FileTempUtil() {
    }

    /**
     * 创建临时文件
     *
     * @param fileName 文件名
     * @param attrs    文件属性
     * @return 临时文件
     */
    public static File createTempFile(String fileName, FileAttribute<?>... attrs) {
        String name = FileNameUtil.getName(fileName);
        String ext = FileNameUtil.getExt(fileName);
        return createTempFile(name, ext, attrs);
    }

    /**
     * 创建临时文件
     *
     * @param fileName 文件名
     * @param bytes    文件内容
     * @param attrs    文件属性
     * @return 临时文件
     */
    public static File createTempFile(String fileName, byte[] bytes, FileAttribute<?>... attrs) {
        File file = createTempFile(fileName, attrs);
        FileUtil.writeFile(file, bytes);
        return file;
    }

    /**
     * 创建临时文件
     *
     * @param fileName 文件名
     * @param ext      文件后缀
     * @param attrs    文件属性
     * @return 临时文件
     */
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

    /**
     * 写入文件到目录中
     *
     * @param prefix 临时目录名称
     * @param name   文件名称
     * @param bytes  文件数据
     * @return 写入的文件
     */
    public static File writeFile2TempDir(String prefix, String name, byte[] bytes, FileAttribute<?>... attrs) {
        Path path = createTempDir(prefix, attrs);
        return writeFile2TempDir(path, name, bytes);
    }

    /**
     * 写入文件到目录中
     *
     * @param path  临时目录路径
     * @param name  文件名称
     * @param bytes 文件数据
     * @return 写入的文件
     */
    public static File writeFile2TempDir(Path path, String name, byte[] bytes) {
        return writeFile2TempDir(path.toFile(), name, bytes);
    }

    /**
     * 写入文件到目录中
     *
     * @param dir   临时目录路径
     * @param name  文件名称
     * @param bytes 文件数据
     * @return 写入的文件
     */
    public static File writeFile2TempDir(File dir, String name, byte[] bytes) {
        File file = new File(dir, name);
        FileUtil.writeFile(file, bytes);
        return file;
    }

    /**
     * 创建临时目录
     *
     * @param prefix 目录名称前缀
     * @param attrs  属性
     * @return 临时目录
     */
    public static Path createTempDir(String prefix, FileAttribute<?>... attrs) {
        try {
            return Files.createTempDirectory(prefix, attrs);
        } catch (Exception e) {
            throw new AppException("Failed to create temporary directory", e);
        }
    }

    /**
     * 创建临时目录
     *
     * @param prefix 目录名称前缀
     * @param name   目录名称
     * @param attrs  属性
     * @return 临时目录
     */
    public static File createTempDir(String prefix, String name, FileAttribute<?>... attrs) {
        File file = createTempDir(prefix, attrs).toFile();
        return FileUtil.mkdir(file, name);
    }

    /**
     * 删除临时文件
     *
     * @param file 文件信息
     */
    public static void deleteTempFile(File file) {
        if (!FileUtil.deleteFile(file)) {
            throw new AppException("Failed to delete temporary file");
        }
    }

    /**
     * 删除所有临时文件
     *
     * @param file 文件
     */
    public static void deleteAllTempFile(File file) {
        if (file.isFile() && file.exists()) {
            deleteTempFile(file);
            return;
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File childFile : files) {
                deleteAllTempFile(childFile);
            }
        }
        deleteTempFile(file);
    }
}
