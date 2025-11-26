package cn.cpoet.patch.assistant;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.StartUpInfo;
import cn.cpoet.patch.assistant.core.TemporaryFileClear;
import cn.cpoet.patch.assistant.util.ExceptionUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.util.ImageUtil;
import cn.cpoet.patch.assistant.util.OSUtil;
import cn.cpoet.patch.assistant.view.home.HomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Arrays;

public class PatchAssistantApplication extends Application {

    @Override
    public void start(Stage stage) {
        OSUtil.initHostServices(getHostServices());
        AppContext appContext = AppContext.getInstance();
        Configuration configuration = appContext.getConfiguration();
        Pane home = StartUpInfo.run(() -> new HomeView(stage).build());
        Scene scene = new Scene(home, configuration.getHomeWidth(), configuration.getHomeHeight());
        appContext.initTheme(scene);
        scene.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setHomeWidth(newVal.doubleValue()));
        scene.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setHomeHeight(newVal.doubleValue()));
        stage.setTitle(I18nUtil.t("app.name", AppConst.APP_NAME) + " By " + AppConst.APP_AUTHOR_NAME);
        stage.getIcons().add(ImageUtil.loadImage(IConConst.APP_ICON));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        AppContext.getInstance().destroy();
    }

    public static void main(String[] args) {
        ExceptionUtil.runAsTryCatch(() -> {
            AppContext.getInstance().initArgs(args);
            TemporaryFileClear.asyncClean();
            launch();
        });
    }
}