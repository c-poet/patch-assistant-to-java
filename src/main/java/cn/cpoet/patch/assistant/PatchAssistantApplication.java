package cn.cpoet.patch.assistant;

import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.view.HomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PatchAssistantApplication extends Application {
    @Override
    public void start(Stage stage) {
        Configuration configuration = cn.cpoet.patch.assistant.core.Application.getInstance().getConfiguration();
        Scene scene = new Scene(new HomeView(stage).build(), configuration.getHomeWidth(), configuration.getHomeHeight());
        stage.widthProperty().addListener((observableValue, oldVal, newVal) -> {
            configuration.setHomeWidth(newVal.doubleValue());
        });
        stage.heightProperty().addListener((observableValue, oldVal, newVal) -> {
            configuration.setHomeHeight(newVal.doubleValue());
        });
        stage.setTitle("补丁助手 By CPoet");
        stage.getIcons().add(new Image("icon.png"));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        cn.cpoet.patch.assistant.core.Application.getInstance().syncConf2File();
    }

    public static void main(String[] args) {
        launch();
    }
}