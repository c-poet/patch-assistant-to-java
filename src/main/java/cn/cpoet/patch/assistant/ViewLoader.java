package cn.cpoet.patch.assistant;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;

/**
 * 视图加载
 *
 * @author CPoet
 */
public class ViewLoader {
    private ViewLoader() {
    }

    public static <T> T load(URL url) throws IOException {
        return FXMLLoader.load(url, null);
    }

    public static <T> T load(String url) throws IOException {
        return load(ViewLoader.class.getResource(url));
    }

    public static Scene load2Scene(String url) throws IOException {
        return new Scene(load(url));
    }

    public static Scene load2Scene(String url, double w, double h) throws IOException {
        return new Scene(load(url), w, h);
    }
}
