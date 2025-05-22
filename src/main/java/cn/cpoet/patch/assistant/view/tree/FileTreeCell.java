package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.DateUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.IConUtil;
import cn.cpoet.patch.assistant.util.ImageUtil;
import cn.cpoet.patch.assistant.view.HomeContext;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * @author CPoet
 */
public class FileTreeCell extends TreeCell<TreeNode> {

    protected HBox box;
    protected final HomeContext homeContext;

    public FileTreeCell(HomeContext homeContext) {
        this.homeContext = homeContext;
    }

    @Override
    public void updateItem(TreeNode node, boolean empty) {
        super.updateItem(node, empty);
        if (empty || node == null) {
            setGraphic(null);
        } else {
            box = new HBox();
            addIcon(node);
            box.getChildren().add(new Label(node.getName()));
            addFileDetail(node);
            setGraphic(box);
        }
    }

    protected void addIcon(TreeNode node) {
        ImageView icon = new ImageView();
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        if (homeContext.isPatchCustomRoot(node)) {
            icon.setImage(ImageUtil.loadImage(IConConst.FILE_MARK));
        } else {
            Image image = IConUtil.loadIconByFileExt(node.getName());
            if (image != null) {
                icon.setImage(image);
            } else if (node instanceof TreeKindNode && ((TreeKindNode) node).isDir()) {
                icon.setImage(ImageUtil.loadImage(IConConst.DIRECTORY));
            } else {
                icon.setImage(ImageUtil.loadImage(IConConst.FILE));
            }
        }
        box.getChildren().add(icon);
    }

    protected void addFileDetail(TreeNode item) {
        if (Boolean.TRUE.equals(Configuration.getInstance().getIsShowFileDetail())) {
            if (item instanceof TreeKindNode) {
                TreeKindNode kindNode = (TreeKindNode) item;
                if (kindNode.isDir()) {
                    return;
                }
                String sizeReadability = FileUtil.getSizeReadability(kindNode.getSize());
                String dateTime = DateUtil.formatDateTime(kindNode.getModifyTime());
                Label fileDetailLbl = new Label("\t" + dateTime + "  " + sizeReadability + "  " + kindNode.getMd5());
                fileDetailLbl.setTextFill(Color.valueOf("#6c707e"));
                box.getChildren().add(fileDetailLbl);
            }
        }
    }
}
