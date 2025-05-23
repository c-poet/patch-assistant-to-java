package cn.cpoet.patch.assistant.view;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 配置界面
 *
 * @author CPoet
 */
public class ConfigView {

    public Node build() {
        VBox vBox = new VBox();
        return vBox;
    }

    public void showDialog(Stage stage) {
        Dialog<Boolean> configViewDialog = new Dialog<>();
        configViewDialog.initOwner(stage);
        configViewDialog.setTitle("配置");
        configViewDialog.setResizable(true);
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(build());
        dialogPane.getButtonTypes().add(ButtonType.APPLY);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        configViewDialog.setDialogPane(dialogPane);
        configViewDialog.showAndWait();
    }
}
