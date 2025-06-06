package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.view.content.ContentParser;
import cn.cpoet.patch.assistant.view.content.ContentSupports;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author CPoet
 */
public abstract class HomeTreeView {

    protected final Stage stage;
    protected final HomeContext context;

    protected HomeTreeView(Stage stage, HomeContext context) {
        this.stage = stage;
        this.context = context;
    }

    protected void selectedLink(TreeView<TreeNode> originTree, TreeView<TreeNode> targetTree) {
        if (!Boolean.TRUE.equals(Configuration.getInstance().getIsSelectedLinked())) {
            return;
        }
        TreeItem<TreeNode> originItem = originTree.getSelectionModel().getSelectedItem();
        if (originItem == null) {
            return;
        }
        TreeNode appNode = originItem.getValue();
        if (appNode.getMappedNode() == null) {
            return;
        }
        TreeItem<TreeNode> targetItem = appNode.getMappedNode().getTreeItem();
        targetTree.getSelectionModel().select(targetItem);
        int targetItemIndex = targetTree.getRow(targetItem);
        targetTree.scrollTo(targetItemIndex);
    }

    protected void doSaveFile(TreeKindNode node, byte[] content, String ext) {
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
        TreeKindNode node = (TreeKindNode) selectedItem.getValue();
        doSaveFile(node, node.getBytes(), FileNameUtil.getExt(node.getText()));
    }

    protected void saveSourceFile(TreeView<TreeNode> treeView) {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || !selectedItem.getValue().getText().endsWith(FileExtConst.DOT_CLASS)) {
            return;
        }
        TreeKindNode node = (TreeKindNode) selectedItem.getValue();
        ContentParser parser = ContentSupports.getContentParser(node);
        if (parser == null) {
            return;
        }
        String content = parser.parse(node);
        doSaveFile(node, content.getBytes(), FileExtConst.JAVA);
    }
}
