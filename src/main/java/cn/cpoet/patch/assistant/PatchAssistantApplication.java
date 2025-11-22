package cn.cpoet.patch.assistant;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.TemporaryFileClear;
import cn.cpoet.patch.assistant.util.ExceptionUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.util.ImageUtil;
import cn.cpoet.patch.assistant.util.OSUtil;
import cn.cpoet.patch.assistant.view.home.HomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PatchAssistantApplication extends Application {

    @Override
    public void start(Stage stage) {
        OSUtil.initHostServices(getHostServices());
        AppContext appContext = AppContext.getInstance();
        Configuration configuration = appContext.getConfiguration();
        Scene scene = new Scene(new HomeView(stage).build(), configuration.getHomeWidth(), configuration.getHomeHeight());
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
            // 禁用 GPU 渲染，强制使用软件渲染
            System.setProperty("prism.order", "sw");  // sw = software
            System.setProperty("prism.forceGPU", "false");

            // 可选：添加更多调试信息
            System.setProperty("prism.verbose", "true");
            System.setProperty("javafx.verbose", "true");

            AppContext.getInstance().initArgs(args);
            TemporaryFileClear.asyncClean();
            launch();
        });
    }
}