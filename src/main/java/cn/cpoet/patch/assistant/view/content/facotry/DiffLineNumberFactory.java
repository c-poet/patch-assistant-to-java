package cn.cpoet.patch.assistant.view.content.facotry;

import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.fxmisc.richtext.GenericStyledArea;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 比较内容行号显示
 *
 * @author CPoet
 * @see org.fxmisc.richtext.LineNumberFactory
 */
public class DiffLineNumberFactory<PS> implements IntFunction<Node> {

    private static final Pattern DIFF_LINE_PATTERN = Pattern.compile("@@\\s+-(\\d+),\\d+\\s+\\+(\\d+),\\d+\\s+@@");
    private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);
    private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
    private static final Font DEFAULT_FONT;
    private static final Background DEFAULT_BACKGROUND;

    private final Val<Integer> nParagraphs;
    private final GenericStyledArea<PS, ?, ?> area;

    static {
        DEFAULT_FONT = Font.font("monospace", FontPosture.ITALIC, 13.0);
        DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.web("#ddd"), null, null));
    }

    public DiffLineNumberFactory(GenericStyledArea<PS, ?, ?> area) {
        this.area = area;
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
    }

    @Override
    public Node apply(int idx) {
        Val<String> formatted = this.nParagraphs.map((n) -> {
            String lineNumber;
            String text = area.getParagraph(idx).getText();
            if (text.startsWith("---") || text.startsWith("+++")) {
                lineNumber = "";
            } else if (text.startsWith("@@")) {
                lineNumber = "...";
            } else {
                lineNumber = String.valueOf(getLineNumber(idx, text.startsWith("-")));
            }
            return format(lineNumber, n);
        });
        Label lineNo = new Label();
        lineNo.setFont(DEFAULT_FONT);
        lineNo.setBackground(DEFAULT_BACKGROUND);
        lineNo.setTextFill(DEFAULT_TEXT_FILL);
        lineNo.setPadding(DEFAULT_INSETS);
        lineNo.setAlignment(Pos.TOP_RIGHT);
        lineNo.getStyleClass().add("lineno");
        lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
        return lineNo;
    }

    protected int getLineNumber(int idx, boolean isReduce) {
        // TODO BY CPoet 每次行号向上查询存在极大的性能损耗
        int lineNumber = 0;
        while (idx > 0) {
            String text = area.getParagraph(--idx).getText();
            if (text != null && text.startsWith("@@")) {
                Matcher matcher = DIFF_LINE_PATTERN.matcher(text);
                if (matcher.matches()) {
                    int line = Integer.parseInt(isReduce ? matcher.group(1) : matcher.group(2));
                    lineNumber = line == 0 ? lineNumber + 1 : line + lineNumber;
                }
                break;
            }
            if (text == null || text.isBlank() || !text.startsWith("-")) {
                if (!isReduce) {
                    ++lineNumber;
                }
            } else if (isReduce) {
                ++lineNumber;
            }
        }
        return lineNumber;
    }

    protected String format(String x, int max) {
        int digits = (int) Math.floor(Math.log10(max)) + 1;
        return String.format("%1$" + digits + "s", x);
    }
}
