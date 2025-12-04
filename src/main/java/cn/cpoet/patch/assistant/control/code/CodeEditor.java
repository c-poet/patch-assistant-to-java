package cn.cpoet.patch.assistant.control.code;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.Virtualized;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author CPoet
 */
public class CodeEditor extends Region implements Virtualized {

    private SearchBox searchBox;
    private final CodeArea codeArea;
    private final StackPane stackPane;

    public CodeEditor() {
        this(new CodeArea());
    }

    public CodeEditor(CodeArea codeArea) {
        getStyleClass().add("code-editor");
        this.codeArea = codeArea;
        stackPane = new StackPane();
        stackPane.setAlignment(Pos.TOP_LEFT);
        getChildren().add(stackPane);
        stackPane.getChildren().add(this.codeArea);
        this.setOnKeyPressed(this::onKeyPressed);
        this.setOnKeyReleased(this::onKeyReleased);
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public void onKeyPressed(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.F) {
                showSearchBox();
                event.consume();
            } else if (event.getCode() == KeyCode.G) {
                GotoRowColDialog gotoRowColDialog = new GotoRowColDialog(this);
                gotoRowColDialog.initOwner(getScene().getWindow());
                gotoRowColDialog.showAndWait();
                event.consume();
            }
        }
    }

    private void showSearchBox() {
        if (searchBox == null) {
            searchBox = new SearchBox(this);
            searchBox.setCloseCallback(this::hideSearchBox);
        }
        if (!stackPane.getChildren().contains(searchBox)) {
            stackPane.getChildren().add(searchBox);
        }
        searchBox.start();
    }

    private void hideSearchBox(SearchBox searchBox) {
        if (searchBox != null) {
            searchBox.end();
            stackPane.getChildren().remove(searchBox);
        }
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

    private void onKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            if (searchBox != null && stackPane.getChildren().contains(searchBox)) {
                hideSearchBox(searchBox);
                event.consume();
            }
        }
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

    @Override
    public Val<Double> totalWidthEstimateProperty() {
        return codeArea.totalWidthEstimateProperty();
    }

    @Override
    public Val<Double> totalHeightEstimateProperty() {
        return codeArea.totalHeightEstimateProperty();
    }

    @Override
    public Var<Double> estimatedScrollXProperty() {
        return codeArea.estimatedScrollXProperty();
    }

    @Override
    public Var<Double> estimatedScrollYProperty() {
        return codeArea.estimatedScrollYProperty();
    }

    @Override
    public void scrollXBy(double v) {
        codeArea.scrollXBy(v);
    }

    @Override
    public void scrollYBy(double v) {
        codeArea.scrollYBy(v);
    }

    @Override
    public void scrollXToPixel(double v) {
        codeArea.scrollXToPixel(v);
    }

    @Override
    public void scrollYToPixel(double v) {
        codeArea.scrollYToPixel(v);
    }
}
