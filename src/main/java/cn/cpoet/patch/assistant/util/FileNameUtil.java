package cn.cpoet.patch.assistant.util;

/**
 * 文件名工具
 *
 * @author CPoet
 */
public abstract class FileNameUtil {

    /**
     * 统一分隔符
     */
    public final static char C_SEPARATOR = '/';

    /**
     * 统一分隔符
     */
    public final static String SEPARATOR = Character.toString(C_SEPARATOR);

    private FileNameUtil() {
    }

    /**
     * 获取文件名（包括后缀）
     *
     * @param path 路径
     * @return 文件名
     */
    public static String getFileName(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        if (path.charAt(path.length() - 1) == C_SEPARATOR) {
            path = path.substring(0, path.length() - 1);
        }
        int i = path.lastIndexOf(C_SEPARATOR);
        return i == -1 ? path : path.substring(i + 1);
    }

    /**
     * 获取文件所在目录路径
     *
     * @param path 文件名
     * @return 目录路径
     */
    public static String getDirPath(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        if (path.charAt(path.length() - 1) == C_SEPARATOR) {
            path = path.substring(0, path.length() - 1);
        }
        int i = path.lastIndexOf(C_SEPARATOR);
        return i == -1 ? "" : path.substring(0, i);
    }
}
