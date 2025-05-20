package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import javafx.scene.image.Image;

/**
 * 图标工具
 *
 * @author CPoet
 */
public abstract class IConUtil {
    private IConUtil() {
    }

    /**
     * 根据文件后缀获取图标
     *
     * @param name 文件名
     * @return 图标
     */
    public static Image loadIconByFileExt(String name) {
        String path = getPathByFileExt(name);
        return path == null ? null : ImageUtil.loadImage(path);
    }

    /**
     * 根据文件后缀获取图标路径
     *
     * @param name 文件名
     * @return 图标路径
     */
    public static String getPathByFileExt(String name) {
        if (name.endsWith(FileExtConst.DOT_JAR)) {
            return IConConst.FILE_JAR;
        } else if (name.endsWith(FileExtConst.DOT_ZIP)) {
            return IConConst.FILE_ZIP;
        } else if (name.endsWith(FileExtConst.DOT_CLASS)) {
            return IConConst.FILE_CLASS;
        } else if (name.endsWith(".js")) {
            return IConConst.FILE_JS;
        } else if (name.endsWith(".html")) {
            return IConConst.FILE_HTML;
        } else if (name.endsWith(".yml")) {
            return IConConst.FILE_YML;
        } else if (name.endsWith(".yaml")) {
            return IConConst.FILE_YAML;
        } else if (name.endsWith(".properties")) {
            return IConConst.FILE_PROPERTIES;
        } else if (name.endsWith(".xml")) {
            return IConConst.FILE_XML;
        } else if (name.endsWith(".docx") || name.endsWith(".doc")) {
            return IConConst.FILE_DOCX;
        } else if (name.endsWith(".pptx") || name.endsWith(".ppt")) {
            return IConConst.FILE_PPTX;
        } else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            return IConConst.FILE_XLSX;
        }
        return null;
    }
}
