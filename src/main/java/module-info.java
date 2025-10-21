module cn.cpoet.patch.assistant {
    requires java.desktop;
    requires javafx.controls;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires engine.main;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires org.fxmisc.flowless;
    requires io.github.javadiffutils;

    exports cn.cpoet.patch.assistant;

    opens cn.cpoet.patch.assistant.core to com.fasterxml.jackson.databind;
    opens cn.cpoet.patch.assistant.model to com.fasterxml.jackson.databind;
}
