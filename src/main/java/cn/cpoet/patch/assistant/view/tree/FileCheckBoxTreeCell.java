package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.view.HomeContext;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;

/**
 * @author CPoet
 */
public class FileCheckBoxTreeCell extends FileTreeCell {

    public FileCheckBoxTreeCell(HomeContext homeContext) {
        super(homeContext);
    }

    @Override
    public void updateItem(TreeNode node, boolean empty) {
        super.updateItem(node, empty);
        if (box != null) {
            if (node != null && node.getMappedNode() != null) {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(node.isChecked());
                checkBox.setOnAction(e -> {
                    TotalInfo totalInfo = context.getTotalInfo();
                    if (node.isChecked()) {
                        totalInfo.decrTotal(node.getStatus());
                        node.setChecked(false);
                    } else {
                        totalInfo.incrTotal(node.getStatus());
                        node.setChecked(true);
                    }
                });
                checkBox.setPadding(new Insets(0, 5, 0, 0));
                box.getChildren().add(0, checkBox);
            }
        }
    }
}
