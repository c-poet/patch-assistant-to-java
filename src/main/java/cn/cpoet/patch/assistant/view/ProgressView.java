package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.control.DialogPurePane;
import cn.cpoet.patch.assistant.core.Configuration;
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

    private final String title;

    public ProgressView(String title) {
        this.title = title;
    }

    public Node build() {
        VBox box = new VBox();
        box.setPadding(Insets.EMPTY);
        progressBar = new ProgressBar();
        progressBar.setPadding(new Insets(2, 1, 2, 1));
        progressBar.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().add(progressBar);
        textArea = new TextArea();
        textArea.setEditable(false);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        box.getChildren().add(textArea);
        return box;
    }

    public void showDialog(Stage stage, Consumer<ProgressContext> consumer) {
        Dialog<?> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        dialog.setTitle(title);
        DialogPane dialogPane = new DialogPurePane();
        dialogPane.setContent(build());
        Configuration configuration = Configuration.getInstance();
        dialogPane.setPrefSize(configuration.getProgressWidth(), configuration.getProgressHeight());
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setProgressWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setProgressHeight(newVal.doubleValue()));
        dialog.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        consumer.accept(this);
        dialog.showAndWait();
    }
}
