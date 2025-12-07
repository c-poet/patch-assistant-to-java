package cn.cpoet.patch.assistant.control.code;

import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.TwoDimensional;

/**
 * @author CPoet
 */
public class GotoRowColDialog extends Dialog<GotoRowColDialog.Pos0> {

    private Pos0 pos;
    private boolean flag;
    private final TextField rowColField;
    private final TextField offsetField;
    private final CodeEditor codeEditor;

    public GotoRowColDialog(CodeEditor codeEditor) {
        this.codeEditor = codeEditor;
        setTitle(I18nUtil.t("app.control.code.goto-row-col-title"));
        HBox rowColBox = new HBox();
        rowColBox.setSpacing(5);
        rowColBox.setAlignment(Pos.CENTER);
        rowColBox.getChildren().add(new Label(I18nUtil.t("app.control.code.goto-row-col-label")));
        rowColField = new TextField();
        rowColField.textProperty().addListener((observableValue, oldVal, newVal) -> handleRowColChange(newVal));
        HBox.setHgrow(rowColField, Priority.ALWAYS);
        rowColBox.getChildren().add(rowColField);

        HBox offsetBox = new HBox();
        offsetBox.setSpacing(5);
        offsetBox.setAlignment(Pos.CENTER);
        offsetBox.getChildren().add(new Label(I18nUtil.t("app.control.code.goto-offset-label")));
        offsetField = new TextField();
        offsetField.textProperty().addListener((observableValue, oldVal, newVal) -> handleOffsetChange(newVal));
        HBox.setHgrow(offsetField, Priority.ALWAYS);
        offsetBox.getChildren().add(offsetField);

        VBox box = new VBox();
        box.setSpacing(10);
        box.getChildren().addAll(rowColBox, offsetBox);

        DialogPane dialogPane = new DialogPane();
        dialogPane.getStyleClass().add("goto-row-col");
        dialogPane.setContent(box);
        setDialogPane(dialogPane);
        setResizable(true);
        dialogPane.setPrefWidth(420);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        initModality(Modality.WINDOW_MODAL);
        setResultConverter(buttonType -> buttonType == ButtonType.OK ? doGotoRowCol() : null);
        setOnShown(e -> {
            CodeArea codeArea = codeEditor.getCodeArea();
            offsetField.setText(String.valueOf(codeArea.getCaretPosition()));
            rowColField.requestFocus();
        });
    }

    private Pos0 doGotoRowCol() {
        if (pos != null) {
            CodeArea codeArea = codeEditor.getCodeArea();
            codeArea.showParagraphAtCenter(pos.row);
            codeArea.moveTo(pos.row, pos.col);
        }
        return pos;
    }

    private void handleRowColChange(String rowCol) {
        if (flag) {
            return;
        }
        flag = true;
        try {
            if (StringUtil.isBlank(rowCol)) {
                clearPos(offsetField);
            } else {
                String[] rowColArr = rowCol.split(":", 2);
                int row = Integer.parseInt(rowColArr[0]);
                int col = rowColArr.length > 1 ? Integer.parseInt(rowColArr[1]) : 0;
                TwoDimensional.Position position = codeEditor.getCodeArea().position(row, col);
                int offset = position.toOffset();
                offsetField.setText(String.valueOf(offset));
                pos = new Pos0(row, col);
            }
        } catch (Exception e) {
            invalidPos(offsetField);
        } finally {
            flag = false;
        }
    }

    private void handleOffsetChange(String offset) {
        if (flag) {
            return;
        }
        flag = true;
        try {
            if (StringUtil.isBlank(offset)) {
                clearPos(rowColField);
            } else {
                int index = Integer.parseInt(offset);
                TwoDimensional.Position position = codeEditor.getCodeArea().offsetToPosition(index, TwoDimensional.Bias.Backward);
                rowColField.setText(position.getMajor() + ":" + position.getMinor());
                pos = new Pos0(position.getMajor(), position.getMinor());
            }
        } catch (Exception e) {
            invalidPos(rowColField);
        } finally {
            flag = false;
        }
    }

    private void clearPos(TextField field) {
        this.pos = null;
        field.clear();
    }

    private void invalidPos(TextField field) {
        clearPos(field);
        field.setPromptText(I18nUtil.t("app.control.code.goto-invalid"));
    }

    public static class Pos0 {

        private int row;
        private int col;

        public Pos0(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }
    }
}
