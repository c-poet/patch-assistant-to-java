package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.view.content.ContentAdapterFactory;
import cn.cpoet.patch.assistant.view.content.IContentAdapter;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.ListModification;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 内容视图
 *
 * @author CPoet
 */
public class ContentView {

    private static final String[] KEYWORDS = new String[]{
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
            + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public Node build(String leftContent, String rightContent, boolean isPatch) {
        CodeArea leftArea = new CodeArea();
        leftArea.setParagraphGraphicFactory(LineNumberFactory.get(leftArea));
        leftArea.setEditable(false);
        leftArea.getVisibleParagraphs().addModificationObserver
                (
                        new VisibleParagraphStyler<>(leftArea, this::computeHighlighting)
                );
        final Pattern whiteSpace = Pattern.compile("^\\s+");
        leftArea.addEventHandler(KeyEvent.KEY_PRESSED, KE ->
        {
            if (KE.getCode() == KeyCode.ENTER) {
                int caretPosition = leftArea.getCaretPosition();
                int currentParagraph = leftArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(leftArea.getParagraph(currentParagraph - 1).getSegments().get(0));
                if (m0.find()) Platform.runLater(() -> leftArea.insertText(caretPosition, m0.group()));
            }
        });
        leftArea.replaceText(leftContent);
        VirtualizedScrollPane<CodeArea> leftPane = new VirtualizedScrollPane<>(leftArea);
        if (rightContent == null) {
            return leftPane;
        }
        CodeArea rightArea = new CodeArea();
        rightArea.setEditable(false);
        rightArea.setParagraphGraphicFactory(LineNumberFactory.get(rightArea));
        rightArea.getVisibleParagraphs().addModificationObserver(new VisibleParagraphStyler<>(rightArea, this::computeHighlighting));
        leftArea.addEventHandler(KeyEvent.KEY_PRESSED, KE ->
        {
            if (KE.getCode() == KeyCode.ENTER) {
                int caretPosition = rightArea.getCaretPosition();
                int currentParagraph = rightArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(rightArea.getParagraph(currentParagraph - 1).getSegments().get(0));
                if (m0.find()) Platform.runLater(() -> rightArea.insertText(caretPosition, m0.group()));
            }
        });
        rightArea.replaceText(rightContent);
        VirtualizedScrollPane<CodeArea> rightPane = new VirtualizedScrollPane<>(rightArea);
        SplitPane splitPane = isPatch ? new SplitPane(rightPane, leftPane) : new SplitPane(leftPane, rightPane);
        String s = FileUtil.readFileAsString("css/java-keywords.css");
        splitPane.setStyle(s);
        return splitPane;
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null; /* never happens */
            assert styleClass != null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    public void showDialog(Stage stage, TreeKindNode node) {
        IContentAdapter adapter = ContentAdapterFactory.defaultFactory().getAdapter(node);
        if (adapter == null) {
            return;
        }
        // BY CPoet 后期增加左右表示
        String leftContent = adapter.handle(node);
        String rightContent = null;
        TreeNode mappedNode = node.getMappedNode();
        if (mappedNode != null) {
            rightContent = adapter.handle((TreeKindNode) mappedNode);
        }
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        dialog.setTitle(node.getPath());
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(this.build(leftContent, rightContent, node.isPatch()));
        dialogPane.setPrefSize(stage.getWidth(), stage.getHeight());
        dialog.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>> {
        private final GenericStyledArea<PS, SEG, S> area;
        private final Function<String, StyleSpans<S>> computeStyles;
        private int prevParagraph, prevTextLength;

        public VisibleParagraphStyler(GenericStyledArea<PS, SEG, S> area, Function<String, StyleSpans<S>> computeStyles) {
            this.computeStyles = computeStyles;
            this.area = area;
        }

        @Override
        public void accept(ListModification<? extends Paragraph<PS, SEG, S>> lm) {
            if (lm.getAddedSize() > 0) Platform.runLater(() ->
            {
                int paragraph = Math.min(area.firstVisibleParToAllParIndex() + lm.getFrom(), area.getParagraphs().size() - 1);
                String text = area.getText(paragraph, 0, paragraph, area.getParagraphLength(paragraph));

                if (paragraph != prevParagraph || text.length() != prevTextLength) {
                    if (paragraph < area.getParagraphs().size() - 1) {
                        int startPos = area.getAbsolutePosition(paragraph, 0);
                        area.setStyleSpans(startPos, computeStyles.apply(text));
                    }
                    prevTextLength = text.length();
                    prevParagraph = paragraph;
                }
            });
        }
    }
}
