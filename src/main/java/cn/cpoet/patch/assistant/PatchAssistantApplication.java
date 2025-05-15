package cn.cpoet.patch.assistant;

import cn.cpoet.patch.assistant.controller.HomeController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class PatchAssistantApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("补丁助手 By CPoet");
        stage.getIcons().add(new Image("icon.png"));
        stage.setScene(HomeController.getView());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}