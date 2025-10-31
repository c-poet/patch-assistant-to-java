package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片工具
 *
 * @author CPoet
 */
public abstract class ImageUtil {

    private static final Map<String, Image> CACHE = new HashMap<>();

    private ImageUtil() {
    }


    public static Image loadImage(String path) {
        try (InputStream in = FileUtil.getFileAsStream(path)) {
            if (in == null) {
                return null;
            }
            return new Image(in);
        } catch (Exception e) {
            throw new AppException("Failed to read the image", e);
        }
    }

    public static Image loadImageCache(String path) {
        Image image = CACHE.get(path);
        if (image != null) {
            return image;
        }
        synchronized (ImageUtil.class) {
            if ((image = CACHE.get(path)) != null) {
                return image;
            }
            image = loadImage(path);
            if (image != null) {
                CACHE.put(path, image);
            }
            return image;
        }
    }
}
