package cn.cpoet.patch.assistant.util;

import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * 图片工具
 *
 * @author CPoet
 */
public abstract class ImageUtil {

    private ImageUtil() {
    }

    public static Image loadImage(String path) {
        try (InputStream in = FileUtil.getFileAsStream(path)) {
            if (in == null) {
                return null;
            }
            return new Image(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
