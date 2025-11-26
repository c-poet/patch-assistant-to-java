package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.view.home.HomeContext;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;

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

    private void createTextField() {
        textField = new TextField(textLbl.getText());
        textField.setStyle("-fx-background-insets: 0; -fx-background-color: transparent, white, transparent, white;");
        textField.setOnAction(event -> {
            TreeNode node = getItem();
            node.setName(textField.getText());
            String dirPath = FileNameUtil.getDirPath(node.getPath());
            node.setPath(FileNameUtil.joinPath(dirPath, node.getName()));
            commitEdit(node);
            event.consume();
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTreeView().isEditable()) {
            return;
        }
        super.startEdit();
        if (isEditing()) {
            if (textField == null) {
                createTextField();
            }
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(box);
        textField = null;
        resetEditable();
    }

    @Override
    public void commitEdit(TreeNode node) {
        super.commitEdit(node);
        textField = null;
        resetEditable();
        // 请求回焦点
        getTreeView().requestFocus();
    }

    private void resetEditable() {
        TreeView<TreeNode> treeView = getTreeView();
        if (treeView instanceof AppTreeView) {
            ((AppTreeView) treeView).resetEditable();
        }
    }

    @Override
    public void updateItem(TreeNode node, boolean empty) {
        if (isEditing()) {
            return;
        }
        super.updateItem(node, empty);
    }
}
