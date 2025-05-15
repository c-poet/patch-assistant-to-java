module cn.cpoet.patch.assistant {
    requires javafx.controls;
    requires javafx.fxml;

    opens cn.cpoet.patch.assistant to javafx.fxml;
    exports cn.cpoet.patch.assistant;
    exports cn.cpoet.patch.assistant.controller;
    opens cn.cpoet.patch.assistant.controller to javafx.fxml;
}