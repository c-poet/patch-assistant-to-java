package cn.cpoet.patch.assistant.control.code;

import javafx.geometry.Insets;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author CPoet
 */
public class CodeEditor extends Region {

    private final CodeArea codeArea;
    private final StackPane stackPane;

    public CodeEditor() {
        this(new CodeArea());
    }

    public CodeEditor(CodeArea codeArea) {
        this.codeArea = codeArea;
        stackPane = new StackPane();
        getChildren().add(stackPane);
        stackPane.getChildren().add(this.codeArea);

        this.setOnKeyPressed(this::onKeyPressed);
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public void onKeyPressed(KeyEvent event) {
        /*if (event.isControlDown() && event.getCode() == KeyCode.F) {
            System.out.println("搜索");
            HBox searchBox = new HBox();
            TextField searchField = new TextField();
            searchBox.getChildren().add(searchField);
            stackPane.getChildren().add(searchBox);
            event.consume();
        }*/
    }

    @Override
    protected void layoutChildren() {
        Insets insets = getInsets();
        double width = getWidth() - insets.getLeft() - insets.getRight();
        double height = getHeight() - insets.getTop() - insets.getBottom();
        stackPane.resizeRelocate(
                insets.getLeft(),
                insets.getTop(),
                width,
                height
        );
    }

    /**
     * 代码编辑器应用代码高亮
     *
     * @param codeArea            代码域
     * @param computeHighlighting 代码高亮计算函数
     */
    public static void applyHighlighting(CodeArea codeArea, Function<String, StyleSpans<Collection<String>>> computeHighlighting) {
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    StyleSpans<Collection<String>> styleSpans = computeHighlighting.apply(codeArea.getText());
                    if (styleSpans != null) {
                        codeArea.setStyleSpans(0, styleSpans);
                    }
                });
    }
}
