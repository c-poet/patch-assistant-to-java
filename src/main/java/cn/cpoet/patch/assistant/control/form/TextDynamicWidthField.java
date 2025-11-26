package cn.cpoet.patch.assistant.control.form;

import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * @author CPoet
 */
public class TextDynamicWidthField extends TextField {

    private final Text text = new Text();

    public TextDynamicWidthField() {
        bindText();
    }

    public TextDynamicWidthField(String text) {
        this();
        setText(text);
    }

    private void bindText() {
        text.fontProperty().bind(fontProperty());
        text.textProperty().bind(textProperty());
        text.textProperty().addListener((observableValue, oldVal, newVal) -> {
            double width = text.getLayoutBounds().getWidth();
            double padding = getPadding().getLeft() + getPadding().getRight() + 2;
            setMaxWidth(width + padding);
        });
    }
}
