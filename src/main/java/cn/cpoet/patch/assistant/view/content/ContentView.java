package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.control.DialogPurePane;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.FileNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.ContentConf;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.OSUtil;
import cn.cpoet.patch.assistant.util.TextDiffUtil;
import cn.cpoet.patch.assistant.view.content.facotry.CharsetChangeEvent;
import cn.cpoet.patch.assistant.view.content.facotry.CodeAreaFactory;
import cn.cpoet.patch.assistant.view.content.facotry.DiffLineNumberFactory;
import cn.cpoet.patch.assistant.view.content.facotry.NodeCodeEditor;
import cn.cpoet.patch.assistant.view.content.parser.ContentParser;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * 内容视图
 *
 * @author CPoet
 */
public class ContentView {

    private String leftContent;
    private String rightContent;
    private DialogPane dialogPane;
    private final TreeNode tarNode;
    private final TreeNode leftNode;
    private final TreeNode rightNode;
    private ContentParser contentParser;
    private CodeAreaFactory codeAreaFactory;

    public ContentView(TreeNode node) {
        tarNode = node;
        if (node.isPatch()) {
            TreeNode mappedNode = node.getMappedNode();
            if (mappedNode != null && !TreeNodeType.ADD.equals(mappedNode.getType())) {
                leftNode = mappedNode;
                rightNode = node;
            } else {
                leftNode = node;
                rightNode = null;
            }
        } else {
            if (TreeNodeType.ADD.equals(node.getType())) {
                leftNode = node.getMappedNode();
                rightNode = null;
            } else {
                leftNode = node;
                rightNode = node.getMappedNode();
            }
        }
    }

    public Node build() {
        codeAreaFactory = ContentSupports.getCodeAreaFactory(leftNode);
        if (!isLoadDiffMode()) {
            VirtualizedScrollPane<NodeCodeEditor> scrollPane = crateCodeAreaPane(leftNode, leftContent);
            scrollPane.scrollYToPixel(0);
            return scrollPane;
        }
        return dynamicCodeAreaWithDiffModel();
    }

    private Node dynamicCodeAreaWithDiffModel() {
        ContentConf contentConf = Configuration.getInstance().getContent();
        Node tarNode;
        if (Boolean.TRUE.equals(contentConf.getDiffModel())) {
            String diff = TextDiffUtil.diff2Str(leftContent, rightContent, leftNode.getPath(), rightNode.getPath());
            VirtualizedScrollPane<NodeCodeEditor> diffPane = crateCodeAreaPane(codeEditor -> {
                CodeArea codeArea = codeEditor.getCodeArea();
                codeArea.setParagraphGraphicFactory(new DiffLineNumberFactory<>(codeArea));
                codeArea.replaceText(diff);
            });
            VBox.setVgrow(diffPane, Priority.ALWAYS);
            tarNode = diffPane;
        } else {
            VirtualizedScrollPane<NodeCodeEditor> leftPane = crateCodeAreaPane(leftNode, leftContent);
            leftPane.scrollYToPixel(0);
            VirtualizedScrollPane<NodeCodeEditor> rightPane = crateCodeAreaPane(rightNode, rightContent);
            rightPane.scrollYToPixel(0);
            SplitPane splitPane = new SplitPane(leftPane, rightPane);
            VBox.setVgrow(splitPane, Priority.ALWAYS);
            splitPane.setPadding(Insets.EMPTY);
            tarNode = splitPane;
        }
        return tarNode;
    }

    private void handleCharsetChange(NodeCodeEditor nodeCodeEditor, CharsetChangeEvent event) {
        Charset charset = event.toCharset();
        TreeNode node = nodeCodeEditor.getNode();
        if (rightNode == null || node == leftNode) {
            leftContent = contentParser.parse(node, charset);
            nodeCodeEditor.getCodeArea().replaceText(leftContent);
        } else {
            rightContent = contentParser.parse(node, charset);
            nodeCodeEditor.getCodeArea().replaceText(rightContent);
        }
    }

    private VirtualizedScrollPane<NodeCodeEditor> crateCodeAreaPane(TreeNode node, String text) {
        return crateCodeAreaPane(codeEditor -> {
            codeEditor.setNode(node);
            codeEditor.getCodeArea().replaceText(text);
        });
    }

    private void handleEditMode(CodeArea codeArea) {
        // codeArea.setEditable(!codeArea.isEditable());
    }

    private VirtualizedScrollPane<NodeCodeEditor> crateCodeAreaPane(Consumer<NodeCodeEditor> consumer) {
        NodeCodeEditor codeEditor = codeAreaFactory.create(isLoadDiffMode());
        codeEditor.addEventHandler(CodeAreaFactory.CHARSET_CHANGE, event -> {
            handleCharsetChange(codeEditor, event);
            event.consume();
        });
        codeEditor.addEventHandler(CodeAreaFactory.SHOW_MODE_CHANGE, event -> {
            Node node = dynamicCodeAreaWithDiffModel();
            dialogPane.setContent(node);
            event.consume();
        });
        codeEditor.addEventHandler(CodeAreaFactory.EDIT_MODE_CHANGE, event -> {
            handleEditMode(codeEditor.getCodeArea());
            event.consume();
        });
        consumer.accept(codeEditor);
        return new VirtualizedScrollPane<>(codeEditor);
    }

    private void openWithSystem() {
        File file;
        if (tarNode instanceof FileNode) {
            file = ((FileNode) tarNode).getFile();
        } else {
            File tempDir = AppContext.getInstance().getTempDir();
            file = new File(tempDir, tarNode.getName());
            FileUtil.writeFile(new File(tempDir, tarNode.getName()), tarNode::consumeBytes);
        }
        OSUtil.openFile(file.getPath());
    }

    private boolean isLoadDiffMode() {
        return rightNode != null;
    }

    public void showDialog(Stage stage) {
        if (leftNode.isDir()) {
            return;
        }
        contentParser = ContentSupports.getContentParser(leftNode);
        if (contentParser == null) {
            openWithSystem();
            return;
        }
        leftContent = contentParser.parse(leftNode);
        if (isLoadDiffMode()) {
            rightContent = contentParser.parse(rightNode);
        }
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.NONE);
        dialog.setResizable(true);
        dialog.setTitle(rightNode == null ? leftNode.getPath() : leftNode.getPath() + " <- " + rightNode.getPath());
        dialogPane = new DialogPurePane();
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
