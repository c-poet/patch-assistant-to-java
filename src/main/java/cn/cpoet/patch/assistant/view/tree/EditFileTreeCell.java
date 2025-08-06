package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.view.HomeContext;
import javafx.scene.control.TextField;

/**
 * 可编辑的树形列
 *
 * @author CPoet
 */
public class EditFileTreeCell extends FileTreeCell {

    private TextField textField;

    public EditFileTreeCell(HomeContext context) {
        super(context);
    }

    protected void initTextField() {
        textField = new TextField(textLbl.getText());
        textField.setStyle("-fx-background-insets: 0; -fx-background-color: transparent, white, transparent, white;");
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (textField == null) {
            initTextField();
        }
        setGraphic(textField);
        textField.selectAll();
        textField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
    }

    @Override
    public void commitEdit(TreeNode newValue) {
        super.commitEdit(newValue);
    }
}
