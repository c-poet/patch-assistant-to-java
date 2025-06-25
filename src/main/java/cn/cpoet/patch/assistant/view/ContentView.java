package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.control.DialogPurePane;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.ContentConf;
import cn.cpoet.patch.assistant.util.TextDiffUtil;
import cn.cpoet.patch.assistant.view.content.CodeAreaFactory;
import cn.cpoet.patch.assistant.view.content.ContentParser;
import cn.cpoet.patch.assistant.view.content.ContentSupports;
import cn.cpoet.patch.assistant.view.content.DiffLineNumberFactory;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.util.function.Consumer;

/**
 * 内容视图
 *
 * @author CPoet
 */
public class ContentView {

    private final TreeNode leftNode;
    private final TreeNode rightNode;
    private String leftContent;
    private String rightContent;

    public ContentView(TreeNode node) {
        if (node.getMappedNode() != null && node.isPatch()) {
            leftNode = node.getMappedNode();
            rightNode = node;
        } else {
            leftNode = node;
            rightNode = node.getMappedNode();
        }
    }

    public Node build() {
        CodeAreaFactory codeAreaFactory = ContentSupports.getCodeAreaFactory(leftNode);
        if (rightContent == null) {
            return crateCodeAreaPane(codeAreaFactory, leftContent);
        }
        VBox box = new VBox();
        box.setPadding(Insets.EMPTY);
        ContentConf contentConf = Configuration.getInstance().getContent();
        ToolBar toolBar = new ToolBar();
        CheckBox diffModeCheckBox = new CheckBox("差异模式");
        diffModeCheckBox.setSelected(Boolean.TRUE.equals(contentConf.getDiffModel()));
        diffModeCheckBox.setOnAction(e -> {
            contentConf.setDiffModel(!Boolean.TRUE.equals(contentConf.getDiffModel()));
            dynamicCodeAreaWithDiffModel(box, contentConf, codeAreaFactory);
        });
        toolBar.getItems().add(diffModeCheckBox);
        box.getChildren().add(toolBar);
        dynamicCodeAreaWithDiffModel(box, contentConf, codeAreaFactory);
        return box;
    }

    private void dynamicCodeAreaWithDiffModel(VBox box, ContentConf contentConf, CodeAreaFactory codeAreaFactory) {
        // TODO By CPoet 后期建立内容缓存，避免内容解析带来的性能损耗
        Node tarNode;
        if (Boolean.TRUE.equals(contentConf.getDiffModel())) {
            String diff = TextDiffUtil.diff2Str(leftContent, rightContent, leftNode.getPath(), rightNode.getPath());
            VirtualizedScrollPane<CodeArea> diffPane = crateCodeAreaPane(codeAreaFactory, codeArea -> {
                codeArea.setParagraphGraphicFactory(new DiffLineNumberFactory<>(codeArea));
                codeArea.replaceText(diff);
            });
            VBox.setVgrow(diffPane, Priority.ALWAYS);
            tarNode = diffPane;
        } else {
            VirtualizedScrollPane<CodeArea> leftPane = crateCodeAreaPane(codeAreaFactory, leftContent);
            VirtualizedScrollPane<CodeArea> rightPane = crateCodeAreaPane(codeAreaFactory, rightContent);
            SplitPane splitPane = new SplitPane(leftPane, rightPane);
            VBox.setVgrow(splitPane, Priority.ALWAYS);
            tarNode = splitPane;
        }
        if (box.getChildren().size() > 1) {
            box.getChildren().set(1, tarNode);
        } else {
            box.getChildren().add(tarNode);
        }
    }

    private VirtualizedScrollPane<CodeArea> crateCodeAreaPane(CodeAreaFactory codeAreaFactory, String text) {
        return crateCodeAreaPane(codeAreaFactory, codeArea -> codeArea.replaceText(text));
    }

    private VirtualizedScrollPane<CodeArea> crateCodeAreaPane(CodeAreaFactory codeAreaFactory, Consumer<CodeArea> consumer) {
        CodeArea codeArea = codeAreaFactory.create();
        consumer.accept(codeArea);
        return new VirtualizedScrollPane<>(codeArea);
    }

    public void showDialog(Stage stage) {
        if (leftNode.isDir()) {
            return;
        }
        ContentParser parser = ContentSupports.getContentParser(leftNode);
        leftContent = parser.parse(leftNode);
        if (rightNode != null) {
            rightContent = parser.parse(rightNode);
        }
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.NONE);
        dialog.setResizable(true);
        dialog.setTitle(rightNode == null ? leftNode.getPath() : leftNode.getPath() + " <- " + rightNode.getPath());
        DialogPane dialogPane = new DialogPurePane();
        dialogPane.setContent(build());
        Configuration configuration = Configuration.getInstance();
        if (configuration.getContentWidth() != null && configuration.getContentHeight() != null) {
            dialogPane.setPrefSize(configuration.getContentWidth(), configuration.getContentHeight());
        } else {
            dialogPane.setPrefSize(stage.getWidth(), stage.getHeight());
        }
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setContentWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setContentHeight(newVal.doubleValue()));
        dialog.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
