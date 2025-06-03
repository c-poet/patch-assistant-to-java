package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.HomeContext;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author CPoet
 */
public class FileTreeCell extends TreeCell<TreeNode> {

    public final static String COPY_FILE_DIR = AppConst.APP_NAME + ".cell-drag";
    public final static ThreadLocal<TreeItem<TreeNode>> COPY_TREE_ITEM = new ThreadLocal<>();

    protected HBox box;
    protected final HomeContext homeContext;

    public FileTreeCell(HomeContext homeContext) {
        this.homeContext = homeContext;
        startCellDrag();
    }

    protected void startCellDrag() {
        setOnDragDetected(e -> {
            if (isEmpty() && !(getItem() instanceof TreeKindNode)) {
                return;
            }
            TreeKindNode node = (TreeKindNode) getItem();
            Dragboard db = this.startDragAndDrop(TransferMode.COPY);
            Image image = getIconImage(node, in -> new Image(in, 68, 68, true, true));
            db.setDragView(image);
            ClipboardContent content = new ClipboardContent();
            content.putString(node.getName());
            File file = FileTempUtil.writeFile2TempDir(COPY_FILE_DIR, node.getName(), node.getBytes());
            content.putFiles(Collections.singletonList(file));
            db.setContent(content);
            COPY_TREE_ITEM.set(getTreeItem());
            e.consume();
        });

        setOnDragOver(e -> {
            Object source = e.getGestureSource();
            if (!(source instanceof FileTreeCell) || isEmpty()) {
                return;
            }
            if (COPY_TREE_ITEM.get() == null
                    || COPY_TREE_ITEM.get().getValue().getMappedNode() != null
                    || COPY_TREE_ITEM.get().getValue().equals(homeContext.getPatchTreeInfo().getReadMeNode())
                    || ((FileTreeCell) e.getGestureSource()).getTreeView() == this.getTreeView()
                    || this.getTreeView() != homeContext.getAppTree()) {
                return;
            }
            e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });

        setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            TreeItem<TreeNode> originItem = COPY_TREE_ITEM.get();
            if (originItem == null || !originItem.getValue().getName().equals(db.getString())) {
                return;
            }
            TreeItem<TreeNode> targetItem = getTreeItem();
            if (!((TreeKindNode) targetItem.getValue()).isDir()) {
                targetItem = targetItem.getParent();
            }
            TreeItem<TreeNode> mappedItem = null;
            for (TreeItem<TreeNode> child : targetItem.getChildren()) {
                if (child.getValue().getName().equals(originItem.getValue().getName())) {
                    mappedItem = child;
                    break;
                }
            }
            TreeKindNode originNode = (TreeKindNode) originItem.getValue();
            if (mappedItem != null) {
                TreeKindNode mappedNode = (TreeKindNode) mappedItem.getValue();
                originNode.setMappedNode(mappedNode);
                originNode.setStatus(TreeNodeStatus.MOD);
                mappedNode.setMappedNode(originNode);
                mappedNode.setStatus(TreeNodeStatus.MOD);
                homeContext.getTotalInfo().incrTotal(TreeNodeStatus.MOD);
            } else {
                VirtualMappedNode virtualMappedNode = new VirtualMappedNode(originNode);
                virtualMappedNode.setStatus(TreeNodeStatus.ADD);
                originNode.setMappedNode(virtualMappedNode);
                originNode.setStatus(TreeNodeStatus.ADD);
                TreeNodeUtil.buildChildNode(targetItem, virtualMappedNode, OnlyChangeFilter.INSTANCE);
                homeContext.getTotalInfo().incrTotal(TreeNodeStatus.ADD);
            }
            Platform.runLater(() -> homeContext.getPatchTree().refresh());
            e.consume();
        });

        setOnDragDone(e -> {
            COPY_TREE_ITEM.remove();
            List<File> files = e.getDragboard().getFiles();
            files.forEach(FileTempUtil::deleteTempFile);
            e.consume();
        });
    }

    @Override
    public void updateItem(TreeNode node, boolean empty) {
        super.updateItem(node, empty);
        if (empty || node == null) {
            setGraphic(null);
        } else {
            box = new HBox();
            addIcon(node);
            box.getChildren().add(new Label(node.getText()));
            addFileDetail(node);
            setGraphic(box);
        }
    }

    protected void addIcon(TreeNode node) {
        ImageView icon = new ImageView();
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        icon.setImage(getIconImage(node, null));
        box.getChildren().add(icon);
    }

    protected Image getIconImage(TreeNode node, Function<InputStream, Image> imgFactory) {
        if (homeContext.isPatchCustomRoot(node)) {
            return ImageUtil.loadImage(IConConst.FILE_MARK, imgFactory);
        }
        Image image = IConUtil.loadIconByFileExt(node.getName(), imgFactory);
        if (image != null) {
            return image;
        }
        if (node instanceof TreeKindNode && ((TreeKindNode) node).isDir()) {
            return ImageUtil.loadImage(IConConst.DIRECTORY, imgFactory);
        }
        return ImageUtil.loadImage(IConConst.FILE, imgFactory);
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
