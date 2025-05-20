package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.view.HomeContext;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;

/**
 * @author CPoet
 */
public class FileCheckBoxTreeCell<T> extends FileTreeCell<T> {

    public FileCheckBoxTreeCell(HomeContext homeContext) {
        super(homeContext);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (box != null) {
            if (item != null && ((TreeNode) item).getMappedNode() != null) {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(Boolean.TRUE.equals(((TreeNode) item).getChecked()));
                checkBox.setOnAction(e -> ((TreeNode) item).setChecked(!Boolean.TRUE.equals(((TreeNode) item).getChecked())));
                checkBox.setPadding(new Insets(0, 5, 0, 0));
                box.getChildren().add(0, checkBox);
            }
        }
    }
}
