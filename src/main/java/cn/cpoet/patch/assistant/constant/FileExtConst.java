package cn.cpoet.patch.assistant.constant;

import cn.cpoet.patch.assistant.util.FileNameUtil;

/**
 * 常用文件后缀
 *
 * @author CPoet
 */
public interface FileExtConst {
    /**
     * zip压缩文件
     */
    String ZIP = "zip";

    /**
     * zip压缩文件
     */
    String DOT_ZIP = FileNameUtil.C_EXT_SEPARATOR + ZIP;

    /**
     * jar文件
     */
    String JAR = "jar";

    /**
     * jar文件
     */
    String DOT_JAR = FileNameUtil.C_EXT_SEPARATOR + JAR;

    /**
     * class文件
     */
    String CLASS = "class";

    /**
     * class文件
     */
    String DOT_CLASS = FileNameUtil.C_EXT_SEPARATOR + CLASS;

    /**
     * java文件
     */
    String JAVA = "java";

    /**
     * java文件
     */
    String DOT_JAVA = FileNameUtil.C_EXT_SEPARATOR + JAVA;

    /**
     * 文本
     */
    String TXT = "txt";

    /**
     * 文本
     */
    String DOT_TXT = FileNameUtil.C_EXT_SEPARATOR + TXT;
}
