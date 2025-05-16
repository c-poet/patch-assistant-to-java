package cn.cpoet.patch.assistant.view.tree;

import javafx.scene.control.CheckBox;

/**
 * @author CPoet
 */
public class FileCheckBoxTreeCell<T> extends FileTreeCell<T> {
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (box != null) {
            box.getChildren().add(0, new CheckBox());
        }
    }
}
