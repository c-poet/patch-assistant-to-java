package cn.cpoet.patch.assistant;

import cn.cpoet.patch.assistant.view.HomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PatchAssistantApplication extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new HomeView(stage).build(), 720, 600);
        stage.setTitle("补丁助手 By CPoet");
        stage.getIcons().add(new Image("icon.png"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}