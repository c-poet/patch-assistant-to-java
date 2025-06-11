package cn.cpoet.patch.assistant;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.ImageUtil;
import cn.cpoet.patch.assistant.view.HomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PatchAssistantApplication extends Application {

    @Override
    public void start(Stage stage) {
        AppContext appContext = AppContext.getInstance();
        Configuration configuration = appContext.getConfiguration();
        Scene scene = new Scene(new HomeView(stage).build(), configuration.getHomeWidth(), configuration.getHomeHeight());
        appContext.initTheme(scene);
        scene.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setHomeWidth(newVal.doubleValue()));
        scene.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setHomeHeight(newVal.doubleValue()));
        stage.setTitle("补丁助手 By CPoet");
        stage.getIcons().add(ImageUtil.loadImage(IConConst.APP_ICON));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        AppContext.getInstance().destroy();
    }

    public static void main(String[] args) {
        AppContext.getInstance().initArgs(args);
        launch();
    }
}