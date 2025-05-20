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

import java.util.zip.ZipEntry;

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
            ImageView icon = new ImageView();
            icon.setFitWidth(16);
            icon.setFitHeight(16);
            String name = ((TreeNode) item).getName();
            if (homeContext.isPatchCustomRoot((TreeNode) item)) {
                icon.setImage(ImageUtil.loadImage(IConConst.FILE_MARK));
            } else {
                Image image = IConUtil.loadIconByFileExt(name);
                if (image != null) {
                    icon.setImage(image);
                } else if (item instanceof ZipEntryNode && ((ZipEntryNode) item).getEntry().isDirectory()) {
                    icon.setImage(ImageUtil.loadImage(IConConst.DIRECTORY));
                } else {
                    icon.setImage(ImageUtil.loadImage(IConConst.FILE));
                }
            }
            box.getChildren().add(icon);
            box.getChildren().add(new Label(name));
            if (Boolean.TRUE.equals(Configuration.getInstance().getIsShowFileDetail())) {
                if (item instanceof ZipEntryNode && !((ZipEntryNode) item).getEntry().isDirectory()) {
                    ZipEntry entry = ((ZipEntryNode) item).getEntry();
                    String sizeReadability = FileUtil.getSizeReadability(entry.getSize());
                    String dateTime = DateUtil.formatDateTime(entry.getTimeLocal());
                    Label fileDetailLbl = new Label("\t" + dateTime + "  " + sizeReadability + "  " + ((ZipEntryNode) item).getMd5());
                    fileDetailLbl.setTextFill(Color.valueOf("#6c707e"));
                    box.getChildren().add(fileDetailLbl);
                }
            }
            setGraphic(box);
        }
    }
}
