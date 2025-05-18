package cn.cpoet.patch.assistant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 文件工具
 *
 * @author CPoet
 */
public abstract class FileUtil {

    private FileUtil() {
    }

    public static String readFileAsString(String path) {
        return "";
    }

    public static byte[] readFile(String path) {
        return "".getBytes();
    }

    public static InputStream getFileAsStream(String path) {
        File file = new File(path);
        try {
            if (file.exists() && file.isFile()) {
                return new FileInputStream(file);
            }
        } catch (Exception ignored) {
        }
        return FileUtil.class.getResourceAsStream(path);
    }
}
