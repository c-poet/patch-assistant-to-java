package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.view.content.CodeAreaFactory;
import cn.cpoet.patch.assistant.view.content.ContentParser;
import cn.cpoet.patch.assistant.view.content.ContentSupports;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.SplitPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

/**
 * 内容视图
 *
 * @author CPoet
 */
public class ContentView {

    public Node build(TreeKindNode node, String leftContent, String rightContent) {
        CodeAreaFactory codeAreaFactory = ContentSupports.getCodeAreaFactory(node);
        CodeArea leftArea = codeAreaFactory.create();
        leftArea.replaceText(leftContent);
        VirtualizedScrollPane<CodeArea> leftPane = new VirtualizedScrollPane<>(leftArea);
        if (rightContent == null) {
            return leftPane;
        }
        CodeArea rightArea = codeAreaFactory.create();
        rightArea.replaceText(rightContent);
        VirtualizedScrollPane<CodeArea> rightPane = new VirtualizedScrollPane<>(rightArea);
        return node.isPatch() ? new SplitPane(rightPane, leftPane) : new SplitPane(leftPane, rightPane);
    }

    public void showDialog(Stage stage, TreeKindNode node) {
        ContentParser parser = ContentSupports.getContentParser(node);
        if (parser == null) {
            return;
        }
        String leftContent = parser.parse(node);
        String rightContent = null;
        TreeNode mappedNode = node.getMappedNode();
        if (mappedNode != null) {
            rightContent = parser.parse((TreeKindNode) mappedNode);
        }
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        dialog.setTitle(node.getPath());
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(build(node, leftContent, rightContent));
        dialogPane.setPrefSize(stage.getWidth(), stage.getHeight());
        dialog.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
