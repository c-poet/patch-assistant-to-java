package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.ImageUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
        Label titleLbl = new Label("补丁助手 Beta");
        titleLbl.setStyle("-fx-font-weight: bold;-fx-font-size:20");
        vBox.getChildren().add(titleLbl);
        Label descLbl = new Label(
                "本工具旨在降低Java应用替换补丁的繁琐性" +
                        "\n补丁包和补丁说明文件（README.txt）需要按照约定的格式进行编辑和排版。" +
                        "\n\n 作者：CPoet" +
                        "\n 邮箱：llzero54@foxmail.com"
        );
        vBox.getChildren().add(descLbl);

        String changelog = FileUtil.readFileAsString(AppConst.CHANGELOG_FILE);
        if (!StringUtil.isBlank(changelog)) {
            TextArea changelogTextArea = new TextArea(changelog);
            changelogTextArea.setEditable(false);
            vBox.getChildren().add(changelogTextArea);
        }

        hBox.getChildren().add(vBox);
        return hBox;
    }

    public void showDialog(Stage stage) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("关于");
        DialogPane dialogPane = new DialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setContent(this.build());
        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
    }
}
