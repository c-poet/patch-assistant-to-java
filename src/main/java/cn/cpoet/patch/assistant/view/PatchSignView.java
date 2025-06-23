package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.model.PatchSign;
import cn.cpoet.patch.assistant.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 查看签名
 *
 * @author CPoet
 */
public class PatchSignView {

    private final PatchSign patchSign;

    public PatchSignView(PatchSign patchSign) {
        this.patchSign = patchSign;
    }

    public Node build() {
        VBox vBox = new VBox();
        vBox.setSpacing(8);
        vBox.getChildren().add(new HBox(new Label("编码："), FXUtil.pre(new TextField(), node -> {
            node.setText("无");
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
        })));
        vBox.getChildren().add(new HBox(new Label("名称："), FXUtil.pre(new TextField(), node -> {
            node.setText(patchSign.getName());
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
        })));
        vBox.getChildren().add(new HBox(new Label("MD5："), FXUtil.pre(new TextField(), node -> {
            node.setText(patchSign.getMd5());
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
        })));
        vBox.getChildren().add(new HBox(new Label("SHA1："), FXUtil.pre(new TextField(), node -> {
            node.setText(patchSign.getSha1());
            node.setEditable(false);
            HBox.setHgrow(node, Priority.ALWAYS);
        })));

        return vBox;
    }

    public void showDialog(Stage stage) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        dialog.setTitle("补丁签名信息");
        DialogPane dialogPane = new DialogPane();
        Configuration configuration = Configuration.getInstance();
        dialogPane.setPrefSize(configuration.getPatchSignWidth(), configuration.getPatchSighHeight());
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setPatchSignWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setPatchSighHeight(newVal.doubleValue()));
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setContent(this.build());
        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
    }
}
