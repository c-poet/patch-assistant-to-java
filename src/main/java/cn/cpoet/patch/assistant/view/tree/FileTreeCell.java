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
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * @author CPoet
 */
public class FileTreeCell<T> extends TreeCell<T> {

    protected HBox box;
    protected final HomeContext homeContext;

    public FileTreeCell(HomeContext homeContext) {
        this.homeContext = homeContext;
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            box = new HBox();
            addIcon(item);
            box.getChildren().add(new Label(((TreeNode) item).getName()));
            addFileDetail(item);
            setGraphic(box);
        }
    }

    protected void addIcon(T item) {
        ImageView icon = new ImageView();
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        if (homeContext.isPatchCustomRoot((TreeNode) item)) {
            icon.setImage(ImageUtil.loadImage(IConConst.FILE_MARK));
        } else {
            Image image = IConUtil.loadIconByFileExt(((TreeNode) item).getName());
            if (image != null) {
                icon.setImage(image);
            } else if (item instanceof ZipEntryNode && ((ZipEntryNode) item).getEntry().isDirectory()) {
                icon.setImage(ImageUtil.loadImage(IConConst.DIRECTORY));
            } else {
                icon.setImage(ImageUtil.loadImage(IConConst.FILE));
            }
        }
        box.getChildren().add(icon);
    }

    protected void addFileDetail(T item) {
        if (Boolean.TRUE.equals(Configuration.getInstance().getIsShowFileDetail())) {
            if (item instanceof ZipEntryNode && !((ZipEntryNode) item).getEntry().isDirectory()) {
                ZipEntryNode zipEntryNode = (ZipEntryNode) item;
                String sizeReadability = FileUtil.getSizeReadability(zipEntryNode.getEntry().getSize());
                String dateTime = DateUtil.formatDateTime(zipEntryNode.getEntry().getTimeLocal());
                Label fileDetailLbl = new Label("\t" + dateTime + "  " + sizeReadability + "  " + zipEntryNode.initAndGetMd5());
                fileDetailLbl.setTextFill(Color.valueOf("#6c707e"));
                box.getChildren().add(fileDetailLbl);
            }
        }
    }
}
