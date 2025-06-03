package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.function.Function;

/**
 * 图片工具
 *
 * @author CPoet
 */
public abstract class ImageUtil {

    private ImageUtil() {
    }

    public static Image loadImage(String path) {
        return loadImage(path, null);
    }

    public static Image loadImage(String path, Function<InputStream, Image> imgFactory) {
        try (InputStream in = FileUtil.getFileAsStream(path)) {
            if (in == null) {
                return null;
            }
            return imgFactory != null ? imgFactory.apply(in) : new Image(in);
        } catch (Exception e) {
            throw new AppException("读取图片失败", e);
        }
    }
}
