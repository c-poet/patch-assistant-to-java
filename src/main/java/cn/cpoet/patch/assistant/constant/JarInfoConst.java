package cn.cpoet.patch.assistant.constant;

/**
 * Jar包相关常量
 *
 * @author CPoet
 */
public interface JarInfoConst {
    /**
     * Jar MetaInf目录
     */
    String META_INFO = "META-INF";

    /**
     * Jar MetaInf目录
     */
    String META_INFO_DIR = META_INFO + "/";

    /**
     * Jar元信息文件
     */
    String MANIFEST_FILE = "MANIFEST.MF";

    /**
     * Jar元信息文件路径
     */
    String MANIFEST_PATH = META_INFO_DIR + MANIFEST_FILE;
}
