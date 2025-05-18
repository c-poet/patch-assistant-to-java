module cn.cpoet.patch.assistant {
    requires javafx.controls;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;

    exports cn.cpoet.patch.assistant;

    opens cn.cpoet.patch.assistant.core to com.fasterxml.jackson.databind;
}