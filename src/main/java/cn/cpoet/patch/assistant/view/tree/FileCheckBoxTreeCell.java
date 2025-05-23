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
                checkBox.setSelected(Boolean.TRUE.equals(node.getChecked()));
                checkBox.setOnAction(e -> {
                    if (Boolean.TRUE.equals(node.getChecked())) {
                        TotalInfo totalInfo = homeContext.getTotalInfo();
                        totalInfo.setModTotal(totalInfo.getModTotal() - 1);
                        node.setChecked(false);
                    } else {
                        TotalInfo totalInfo = homeContext.getTotalInfo();
                        totalInfo.setModTotal(totalInfo.getModTotal() + 1);
                        node.setChecked(true);
                    }
                });
                checkBox.setPadding(new Insets(0, 5, 0, 0));
                box.getChildren().add(0, checkBox);
            }
        }
    }
}
