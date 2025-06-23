package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.jdk.SortLinkedList;
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
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            if (isEmpty() || getItem() == null) {
                return;
            }
            TreeNode node = getItem();
            Dragboard db = this.startDragAndDrop(TransferMode.COPY);
            Image image = getIconImage(node, in -> new Image(in, 68, 68, true, true));
            db.setDragView(image);
            ClipboardContent content = new ClipboardContent();
            content.putString(node.getName());

            List<File> files = getTreeView().getSelectionModel().getSelectedItems()
                    .stream()
                    .map(TreeItem::getValue)
                    .map(treeNode -> {
                        if (treeNode.isDir()) {
                            File file = FileTempUtil.createTempDir(COPY_FILE_DIR, treeNode.getName());
                            deepWriteFile2Path(file, treeNode);
                            return file;
                        }
                        return FileTempUtil.writeFile2TempDir(COPY_FILE_DIR, treeNode.getName(), treeNode.getBytes());
                    }).collect(Collectors.toList());

            content.putFiles(files);
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
            if (!targetItem.getValue().isDir()) {
                targetItem = targetItem.getParent();
            }
            TreeItem<TreeNode> mappedItem = null;
            for (TreeItem<TreeNode> child : targetItem.getChildren()) {
                if (child.getValue().getName().equals(originItem.getValue().getName())) {
                    mappedItem = child;
                    break;
                }
            }
            TreeNode originNode = originItem.getValue();
            if (mappedItem != null) {
                TreeNode mappedNode = mappedItem.getValue();
                TreeNodeUtil.mappedNode(homeContext.getTotalInfo(), originNode, mappedNode, TreeNodeStatus.MOD);
            } else {
                VirtualMappedNode virtualMappedNode = new VirtualMappedNode(originNode);
                virtualMappedNode.setParent(targetItem.getValue());
                TreeNodeUtil.mappedNode(homeContext.getTotalInfo(), originNode, virtualMappedNode, TreeNodeStatus.ADD);
                List<TreeNode> children = targetItem.getValue().getAndInitChildren();
                if (children instanceof SortLinkedList) {
                    int index = ((SortLinkedList<TreeNode>) children).addAndIndex(virtualMappedNode);
                    TreeNodeUtil.buildChildNode(targetItem, index, virtualMappedNode, OnlyChangeFilter.INSTANCE);
                } else {
                    children.add(virtualMappedNode);
                    TreeNodeUtil.buildChildNode(targetItem, virtualMappedNode, OnlyChangeFilter.INSTANCE);
                }
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

    protected void deepWriteFile2Path(File parent, TreeNode node) {
        if (CollectionUtil.isEmpty(node.getChildren())) {
            return;
        }
        for (TreeNode childNode : node.getChildren()) {
            if (childNode.isDir()) {
                File childDir = FileUtil.mkdir(parent, childNode.getName());
                deepWriteFile2Path(childDir, childNode);
                continue;
            }
            FileUtil.writeFile(new File(parent, childNode.getName()), childNode.getBytes());
            if (CollectionUtil.isNotEmpty(childNode.getChildren())) {
                deepWriteFile2Path(parent, childNode);
            }
        }
    }

    @Override
    public void updateItem(TreeNode node, boolean empty) {
        super.updateItem(node, empty);
        if (empty || node == null) {
            setText(null);
            setGraphic(null);
        } else {
            box = new HBox();
            addIcon(node);
            Label textLbl = new Label(node.getText());
            fillTextColor(node, textLbl);
            box.getChildren().add(textLbl);
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
        if (node.isDir()) {
            return ImageUtil.loadImage(IConConst.DIRECTORY, imgFactory);
        }
        return ImageUtil.loadImage(IConConst.FILE, imgFactory);
    }

    protected void addFileDetail(TreeNode node) {
        if (Boolean.TRUE.equals(Configuration.getInstance().getIsShowFileDetail())) {
            if (node.isDir()) {
                return;
            }
            String sizeReadability = FileUtil.getSizeReadability(node.getSize());
            String dateTime = DateUtil.formatDateTime(node.getModifyTime());
            Label fileDetailLbl = new Label("\t" + dateTime + "  " + sizeReadability + "  " + node.getMd5());
            fileDetailLbl.setTextFill(Color.web("#6c707e"));
            box.getChildren().add(fileDetailLbl);
        }
    }

    protected void fillTextColor(TreeNode node, Label textLbl) {
        TreeNodeStatus status = node.getStatus();
        switch (status) {
            case ADD:
                textLbl.setTextFill(Color.web("#4fc75c"));
                break;
            case MOD:
                textLbl.setTextFill(Color.web("#4c89fb"));
                break;
            case DEL:
                textLbl.setTextFill(Color.web("#979797"));
                break;
            case MANUAL_DEL:
                textLbl.setTextFill(Color.web("#e65256"));
                break;
            case NONE:
            default:
        }
    }
}
