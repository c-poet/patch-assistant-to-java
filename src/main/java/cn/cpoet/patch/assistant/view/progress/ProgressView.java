package cn.cpoet.patch.assistant.view.progress;

import cn.cpoet.patch.assistant.control.DialogPurePane;
import cn.cpoet.patch.assistant.util.AlterUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * 进度日志显示
 *
 * @author CPoet
 */
public class ProgressView extends ProgressContext {

    private final String taskName;

    public ProgressView(String taskName) {
        this.taskName = taskName;
    }

    public Node build() {
        VBox box = new VBox();
        box.setPadding(Insets.EMPTY);
        progressBar = new ProgressBar();
        progressBar.setPadding(new Insets(2, 1, 2, 1));
        progressBar.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().add(progressBar);
        textField = new TextField();
        textField.setEditable(false);
        textField.setStyle("-fx-border-color: transparent; " +
                "-fx-border-width: 0; " +
                "-fx-background-color: transparent; " +
                "-fx-background-insets: 0; " +
                "-fx-focus-color: transparent; " +
                "-fx-faint-focus-color: transparent;");
        VBox.setVgrow(textField, Priority.ALWAYS);
        box.getChildren().add(textField);
        return box;
    }

    public void showDialog(Stage stage, Consumer<ProgressContext> consumer) {
        dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(I18nUtil.t("app.view.progress.executing-title") + taskName);
        DialogPane dialogPane = new DialogPurePane();
        dialogPane.setContent(build());
        dialogPane.setPrefWidth(580);
        dialog.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);
        consumer.accept(this);
        dialogPane.getScene().getWindow().setOnCloseRequest(e -> {
            if (!isEnd()) {
                AlterUtil.error(stage, "【" + taskName + "】" + I18nUtil.t("app.view.progress.executing-tip"));
                e.consume();
            }
        });
        dialog.showAndWait();
    }
}
