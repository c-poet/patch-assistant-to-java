package cn.cpoet.patch.assistant.view.tree;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;

/**
 * @author CPoet
 */
public class FileTreeCell<T> extends TreeCell<T> {

    protected HBox box;

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            this.box = new HBox(new Label(((FileNode) item).getName()));
            setGraphic(box);
        }
    }
}
