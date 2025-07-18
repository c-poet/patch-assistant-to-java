package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.content.ContentParser;
import cn.cpoet.patch.assistant.view.content.ContentSupports;
import cn.cpoet.patch.assistant.view.tree.AppTreeView;
import cn.cpoet.patch.assistant.view.tree.CustomTreeView;
import cn.cpoet.patch.assistant.view.tree.PatchTreeView;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author CPoet
 */
public abstract class HomeTreeView {

    /** 选中联动标记，避免重复进入 */
    private static final ThreadLocal<Boolean> SELECTED_LINK_FLAG_TL = new ThreadLocal<>();

    protected final Stage stage;
    protected final HomeContext context;
    protected final AppTreeView appTree;
    protected final PatchTreeView patchTree;

    protected HomeTreeView(Stage stage, HomeContext context) {
        this.stage = stage;
        this.context = context;
        this.appTree = context.getAppTree();
        this.patchTree = context.getPatchTree();
    }

    protected void cancelMapped(CustomTreeView<?> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        TreeNode selectedNode = selectedItem.getValue();
        TreeNode mappedNode = selectedNode.getMappedNode();
        TreeNodeUtil.cleanMappedNode(context.totalInfo, selectedNode);
        TreeNodeUtil.cleanMappedNode(mappedNode);
        appTree.refresh();
        patchTree.refresh();
    }

    protected void selectedLink(TreeView<TreeNode> originTree, TreeView<TreeNode> targetTree) {
        if (!Boolean.TRUE.equals(Configuration.getInstance().getIsSelectedLinked())) {
            return;
        }
        if (Boolean.TRUE.equals(SELECTED_LINK_FLAG_TL.get())) {
            return;
        }
        try {
            SELECTED_LINK_FLAG_TL.set(Boolean.TRUE);
            TreeItem<TreeNode> originItem = originTree.getSelectionModel().getSelectedItem();
            if (originItem == null) {
                return;
            }
            TreeNode appNode = originItem.getValue();
            if (appNode.getMappedNode() == null) {
                return;
            }
            TreeItem<TreeNode> targetItem = appNode.getMappedNode().getTreeItem();
            MultipleSelectionModel<TreeItem<TreeNode>> selectionModel = targetTree.getSelectionModel();
            selectionModel.clearSelection();
            selectionModel.select(targetItem);
            int targetItemIndex = targetTree.getRow(targetItem);
            targetTree.scrollTo(targetItemIndex);
        } finally {
            SELECTED_LINK_FLAG_TL.remove();
        }
    }

    protected void doSaveFile(TreeNode node, byte[] content, String ext) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        String name = FileNameUtil.getName(FileNameUtil.getFileName(node.getText()));
        if (ext == null) {
            fileChooser.setInitialFileName(name);
        } else {
            fileChooser.setInitialFileName(name + FileNameUtil.C_EXT_SEPARATOR + ext);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext.toUpperCase() + "文件", "*." + ext));
        }
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        FileUtil.writeFile(file, content);
    }

    protected void saveFile(TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        TreeNode node = selectedItem.getValue();
        doSaveFile(node, node.getBytes(), FileNameUtil.getExt(node.getText()));
    }

    protected void saveSourceFile(TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || !selectedItem.getValue().getText().endsWith(FileExtConst.DOT_CLASS)) {
            return;
        }
        TreeNode node = selectedItem.getValue();
        ContentParser parser = ContentSupports.getContentParser(node);
        String content = parser.parse(node);
        doSaveFile(node, content.getBytes(), FileExtConst.JAVA);
    }

    protected boolean isDragFromTree(DragEvent event) {
        return event.getGestureSource() instanceof TreeCell;
    }
}
