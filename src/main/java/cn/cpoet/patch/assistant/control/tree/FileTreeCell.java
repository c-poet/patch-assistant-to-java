package cn.cpoet.patch.assistant.control.tree;

import cn.cpoet.patch.assistant.constant.StyleConst;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.home.HomeContext;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @author CPoet
 */
public class FileTreeCell extends TreeCell<TreeNode> {

    public final static ThreadLocal<FileTreeCellDragInfo> DRAG_INFO_TL = new ThreadLocal<>();

    protected HBox box;
    protected Label textLbl;
    protected AppTreeView appTree;
    protected PatchTreeView patchTree;
    protected final HomeContext context;

    public FileTreeCell(HomeContext context) {
        this.appTree = context.getAppTree();
        this.patchTree = context.getPatchTree();
        this.context = context;
        initCellDrag();
    }

    private void onDragDetected(MouseEvent event) {
        if (isEmpty() || getItem() == null) {
            return;
        }
        TreeNode node = getItem();
        Dragboard db = startDragAndDrop(TransferMode.COPY);
        db.setDragView(getDragIconImage(node));
        ClipboardContent content = new ClipboardContent();
        content.putString(node.getName());
        FileTreeCellDragInfo dragInfo = new FileTreeCellDragInfo();
        dragInfo.setOriginTree((CustomTreeView<?>) getTreeView());
        List<TreeNode> patchNodes = getTreeView().getSelectionModel().getSelectedItems()
                .stream()
                .map(TreeItem::getValue)
                .collect(Collectors.toList());
        dragInfo.setTreeNodes(patchNodes);
        List<File> files = getDragFiles(patchNodes, dragInfo);
        content.putFiles(files);
        db.setContent(content);
        DRAG_INFO_TL.set(dragInfo);
        event.consume();
    }

    private List<File> getDragFiles(List<TreeNode> patchNodes, FileTreeCellDragInfo dragInfo) {
        File dragTempDir = FileUtil.mkdir(AppContext.getInstance().getTempDir(), UUIDUtil.random32());
        dragInfo.addTempFile(dragTempDir);
        return patchNodes.stream().map(patchNode -> {
            if (patchNode.isDir()) {
                File file = FileUtil.mkdir(dragTempDir, patchNode.getName());
                updateCellDragInfo(dragInfo, patchNode, file);
                deepWriteFile2Path(file, patchNode, dragInfo);
                return file;
            }
            File file = new File(dragTempDir, patchNode.getName());
            FileUtil.writeFile(file, patchNode::consumeBytes);
            updateCellDragInfo(dragInfo, patchNode, file);
            return file;
        }).collect(Collectors.toList());
    }

    private void onDragOver(DragEvent event) {
        Object source = event.getGestureSource();
        if (isEmpty() || TreeNodeType.ROOT.equals(getItem().getType())) {
            return;
        }
        if (source != null) {
            if (!(source instanceof FileTreeCell)) {
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
        }
        event.acceptTransferModes(TransferMode.COPY);
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        FileTreeCellDragInfo dragInfo = DRAG_INFO_TL.get();
        if (isEmpty() || TreeNodeType.ROOT.equals(getItem().getType()) || appTree != getTreeView()) {
            return;
        }
        if (dragInfo == null) {
            List<File> files = event.getDragboard().getFiles();
            UIUtil.runNotUI(() -> {
                new TreeNodeFileMatchProcessor(context.getTotalInfo(), appTree.getTreeInfo(), getTreeItem().getValue(), files).exec();
                UIUtil.runUI(() -> {
                    appTree.refresh();
                    patchTree.refresh();
                });
            });
        } else {
            UIUtil.runNotUI(() -> {
                new TreeNodeTreeMatchProcessor(context.getTotalInfo(), appTree.getTreeInfo(), getTreeItem().getValue(), dragInfo.getTreeNodes()).exec();
                UIUtil.runUI(() -> {
                    appTree.refresh();
                    patchTree.refresh();
                });
            });
        }
        event.consume();
    }

    private void onDragDone(DragEvent event) {
        FileTreeCellDragInfo dragInfo = DRAG_INFO_TL.get();
        if (dragInfo == null) {
            return;
        }
        DRAG_INFO_TL.remove();
        UIUtil.runNotUI(() -> {
            Stack<File> tempFileStack = dragInfo.getTempFileStack();
            while (!tempFileStack.empty()) {
                FileTempUtil.deleteTempFile(tempFileStack.pop());
            }
        });
        event.consume();
    }

    private void initCellDrag() {
        setOnDragDetected(this::onDragDetected);
        setOnDragOver(this::onDragOver);
        setOnDragDropped(this::onDragDropped);
        setOnDragDone(this::onDragDone);
    }

    private void updateCellDragInfo(FileTreeCellDragInfo dragInfo, TreeNode node, File file) {
        dragInfo.addTempFile(file);
        if (node.getMappedNode() != null) {
            dragInfo.setHasMappedNode(true);
        } else if (TreeNodeType.README.equals(node.getType())) {
            dragInfo.setHasReadmeNode(true);
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
            FileUtil.writeFile(file, childNode::consumeBytes);
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
            textLbl = new Label(node.getName());
            textLbl.getStyleClass().add("title");
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
        icon.setImage(TreeNodeUtil.getIconImage(node));
        box.getChildren().add(icon);
    }

    private Image getDragIconImage(TreeNode node) {
        ImageView imageView = new ImageView(TreeNodeUtil.getIconImage(node));
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        imageView.setPreserveRatio(false);
        return imageView.snapshot(new SnapshotParameters(), null);
    }

    private void addFileDetail(TreeNode node) {
        if (Boolean.TRUE.equals(Configuration.getInstance().getIsShowFileDetail())) {
            if (node.isDir()) {
                return;
            }
            String sizeReadability = FileUtil.getSizeReadability(node.getSizeOrInit());
            String dateTime = DateUtil.formatDateTime(node.getModifyTime());
            Label fileDetailLbl = new Label("\t" + dateTime + "  " + sizeReadability + "  " + node.getMd5OrInit());
            fileDetailLbl.getStyleClass().add("detail");
            fileDetailLbl.setTextFill(StyleConst.COLOR_GRAY_1);
            box.getChildren().add(fileDetailLbl);
        }
    }

    private void fillTextColor(TreeNode node, Label textLbl) {
        TreeNodeType status = node.getType();
        switch (status) {
            case ADD:
                textLbl.setTextFill(StyleConst.COLOR_GREEN);
                break;
            case MOD:
                textLbl.setTextFill(StyleConst.COLOR_BLUE);
                break;
            case DEL:
                textLbl.setTextFill(StyleConst.COLOR_GRAY);
                break;
            default:
        }
    }
}
