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

    /**
     * 后缀名分隔符
     */
    public final static char C_EXT_SEPARATOR = '.';

    /**
     * 后缀名分隔符
     */
    public final static String EXT_SEPARATOR = Character.toString(C_EXT_SEPARATOR);

    private FileNameUtil() {
    }

    public static String getName(String fileName) {
        int i = fileName.lastIndexOf(C_EXT_SEPARATOR);
        return i == -1 ? fileName : fileName.substring(0, i);
    }

    public static String getExt(String fileName) {
        int i = fileName.lastIndexOf(C_EXT_SEPARATOR);
        return i == -1 ? null : fileName.substring(i + 1);
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

    /**
     * 拼接路径
     *
     * @param path1 路径1
     * @param path2 路径2
     * @return 拼接后的路径
     */
    public static String joinPath(String path1, String path2) {
        if (path1.endsWith(SEPARATOR)) {
            return path1 + (path2.startsWith(SEPARATOR) ? path2.substring(SEPARATOR.length()) : path2);
        }
        return path1 + (path2.startsWith(SEPARATOR) ? path2 : SEPARATOR + path2);
    }
}
