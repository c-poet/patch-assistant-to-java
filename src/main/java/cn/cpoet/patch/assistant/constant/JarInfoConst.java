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

    /**
     * 补丁操作记录路径
     */
    String PATCH_UP_PATH = JarInfoConst.META_INFO_DIR + AppConst.PATCH_UP_DIR + "/";

    /**
     * 项目源码目录
     */
    String SOURCE_SRC = "src";

    /**
     * 项目依赖目录
     */
    String SOURCE_LIB = "lib";

    /**
     * pom文件
     */
    String MVN_POM_XML = "pom.xml";

    /**
     * pom文件
     */
    String MVN_POM_PROPERTIES = "pom.properties";
}
