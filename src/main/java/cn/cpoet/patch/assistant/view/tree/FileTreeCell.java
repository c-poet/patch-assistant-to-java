package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.HomeContext;
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
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author CPoet
 */
public class FileTreeCell extends TreeCell<TreeNode> {

    public final static String COPY_FILE_DIR = AppConst.APP_NAME + ".cell-drag";
    public final static ThreadLocal<FileTreeCellDragInfo> DRAG_INFO_TL = new ThreadLocal<>();

    protected HBox box;
    protected AppTreeView appTree;
    protected PatchTreeView patchTree;
    protected final HomeContext context;

    public FileTreeCell(HomeContext context) {
        this.appTree = context.getAppTree();
        this.patchTree = context.getPatchTree();
        this.context = context;
        startCellDrag();
    }

    private void startCellDrag() {
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
            FileTreeCellDragInfo dragInfo = new FileTreeCellDragInfo();
            dragInfo.setOriginTree((CustomTreeView<?>) getTreeView());
            List<TreeNode> patchNodes = getTreeView().getSelectionModel().getSelectedItems()
                    .stream()
                    .map(TreeItem::getValue)
                    .collect(Collectors.toList());
            dragInfo.setTreeNodes(patchNodes);
            List<File> files = patchNodes.stream().map(patchNode -> {
                if (patchNode.isDir()) {
                    File file = FileTempUtil.createTempDir(COPY_FILE_DIR, patchNode.getName());
                    updateCellDragInfo(dragInfo, patchNode, file);
                    deepWriteFile2Path(file, patchNode, dragInfo);
                    return file;
                }
                File file = FileTempUtil.writeFile2TempDir(COPY_FILE_DIR, patchNode.getName(), patchNode.getBytes());
                updateCellDragInfo(dragInfo, patchNode, file);
                return file;
            }).collect(Collectors.toList());
            content.putFiles(files);
            db.setContent(content);
            DRAG_INFO_TL.set(dragInfo);
            e.consume();
        });

        setOnDragOver(e -> {
            Object source = e.getGestureSource();
            if (!(source instanceof FileTreeCell) || isEmpty()) {
                return;
            }
            FileTreeCellDragInfo dragInfo = DRAG_INFO_TL.get();
            if (dragInfo == null
                    || dragInfo.isHasMappedNode()
                    || dragInfo.isHasReadmeNode()
                    || dragInfo.getOriginTree() == getTreeView()
                    || getTreeView() != appTree) {
                return;
            }
            e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });

        setOnDragDropped(e -> {
            FileTreeCellDragInfo dragInfo = DRAG_INFO_TL.get();
            if (dragInfo == null || appTree != getTreeView()) {
                return;
            }
            new TreeNodeMatchProcessor(context.getTotalInfo(), getTreeItem().getValue(), dragInfo.getTreeNodes()).exec();
            appTree.refresh();
            patchTree.refresh();
            e.consume();
        });

        setOnDragDone(e -> {
            FileTreeCellDragInfo dragInfo = DRAG_INFO_TL.get();
            if (dragInfo == null) {
                return;
            }
            DRAG_INFO_TL.remove();
            Stack<File> tempFileStack = dragInfo.getTempFileStack();
            while (!tempFileStack.empty()) {
                FileTempUtil.deleteTempFile(tempFileStack.pop());
            }
            e.consume();
        });
    }

    private void updateCellDragInfo(FileTreeCellDragInfo dragInfo, TreeNode node, File file) {
        dragInfo.addTempFile(file);
        if (node.getMappedNode() != null) {
            dragInfo.setHasMappedNode(true);
        } else if (TreeNodeStatus.README.equals(node.getStatus())) {
            dragInfo.setHasMappedNode(true);
        }
    }

    private void deepWriteFile2Path(File parent, TreeNode node, FileTreeCellDragInfo dragInfo) {
        if (CollectionUtil.isEmpty(node.getChildren())) {
            return;
        }
        for (TreeNode childNode : node.getChildren()) {
            if (childNode.isDir()) {
                File childDir = FileUtil.mkdir(parent, childNode.getName());
                updateCellDragInfo(dragInfo, childNode, childDir);
                deepWriteFile2Path(childDir, childNode, dragInfo);
                continue;
            }
            File file = new File(parent, childNode.getName());
            FileUtil.writeFile(file, childNode.getBytes());
            updateCellDragInfo(dragInfo, childNode, file);
            if (CollectionUtil.isNotEmpty(childNode.getChildren())) {
                deepWriteFile2Path(parent, childNode, dragInfo);
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

    private void addIcon(TreeNode node) {
        ImageView icon = new ImageView();
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        icon.setImage(getIconImage(node, null));
        box.getChildren().add(icon);
    }

    private Image getIconImage(TreeNode node, Function<InputStream, Image> imgFactory) {
        if (context.isPatchCustomRoot(node)) {
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

    private void addFileDetail(TreeNode node) {
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

    private void fillTextColor(TreeNode node, Label textLbl) {
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
