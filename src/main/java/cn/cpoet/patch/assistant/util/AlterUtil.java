package cn.cpoet.patch.assistant.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * 对话框工具
 *
 * @author CPoet
 */
public abstract class AlterUtil {
    private AlterUtil() {
    }

    public static ButtonType info(String message, ButtonType... buttonTypes) {
        return info(null, message, buttonTypes);
    }

    public static ButtonType info(Stage stage, String message, ButtonType... buttonTypes) {
        return show(stage, Alert.AlertType.INFORMATION, message, buttonTypes);
    }

    public static ButtonType warn(String message, ButtonType... buttonTypes) {
        return warn(null, message, buttonTypes);
    }

    public static ButtonType warn(Stage stage, String message, ButtonType... buttonTypes) {
        return show(stage, Alert.AlertType.WARNING, message, buttonTypes);
    }

    public static ButtonType error(String message, ButtonType... buttonTypes) {
        return error(null, message, buttonTypes);
    }

    public static ButtonType error(Stage stage, String message, ButtonType... buttonTypes) {
        return show(stage, Alert.AlertType.ERROR, message, buttonTypes);
    }

    public static ButtonType confirm(String message, ButtonType... buttonTypes) {
        return error(null, message, buttonTypes);
    }

    public static ButtonType confirm(Stage stage, String message, ButtonType... buttonTypes) {
        return show(stage, Alert.AlertType.CONFIRMATION, message, buttonTypes);
    }

    private static ButtonType show(Stage stage, Alert.AlertType type, String message, ButtonType... buttonTypes) {
        Alert alert = new Alert(type, message, buttonTypes);
        if (stage != null) {
            alert.initOwner(stage);
        }
        alert.setTitle(I18nUtil.t("app.alter.title-" + type.name().toLowerCase()));
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(null);
    }
}
