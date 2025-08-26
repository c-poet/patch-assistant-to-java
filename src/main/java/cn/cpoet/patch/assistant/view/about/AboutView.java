package cn.cpoet.patch.assistant.view.about;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.model.Application;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.util.ImageUtil;
import cn.cpoet.patch.assistant.util.XMLUtil;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * 关于页面
 *
 * @author CPoet
 */
public class AboutView {

    public Node build() {
        HBox hBox = new HBox();
        Image appIcon = ImageUtil.loadImage(IConConst.APP_ICON);
        ImageView imageView = new ImageView(appIcon);
        imageView.setFitWidth(68);
        imageView.setFitHeight(68);
        hBox.getChildren().add(imageView);
        VBox vBox = new VBox();
        vBox.setSpacing(8);
        Application applicationInfo = getApplicationInfo();
        Label titleLbl = new Label(I18nUtil.t("app.name", AppConst.APP_NAME) + " " + (applicationInfo == null ? "Beta" : applicationInfo.getVersion()));
        titleLbl.setStyle("-fx-font-weight: bold;-fx-font-size:20");
        vBox.getChildren().add(titleLbl);
        if (applicationInfo != null) {
            Label buildInfoLbl = new Label("Build #" + applicationInfo.getBuildTime() + " " + applicationInfo.getArtifactId());
            buildInfoLbl.setTextFill(Color.web("#25324d"));
            vBox.getChildren().add(buildInfoLbl);
        }
        Label descLbl = new Label(I18nUtil.t("app.view.about.desc")
                + "\n\n " + I18nUtil.t("app.view.about.author")
                + AppConst.APP_AUTHOR_NAME + "\n " + I18nUtil.t("app.view.about.email") + AppConst.APP_AUTHOR_EMAIL);
        vBox.getChildren().add(descLbl);
        hBox.getChildren().add(vBox);
        return hBox;
    }

    private Application getApplicationInfo() {
        try (InputStream in = FileUtil.getFileAsStream(AppConst.APPLICATION_INFO_NAME)) {
            return XMLUtil.read(in, Application.class);
        } catch (Exception ignored) {
        }
        return null;
    }

    public void showDialog(Stage stage) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(I18nUtil.t("app.view.about.title"));
        DialogPane dialogPane = new DialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setContent(this.build());
        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
    }
}
