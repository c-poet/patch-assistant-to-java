package cn.cpoet.patch.assistant.control.code;

import cn.cpoet.patch.assistant.constant.IConFontConst;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.util.UIUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.TwoDimensional;

import java.util.function.Consumer;

/**
 * 搜索框
 *
 * @author CPoet
 */
public class SearchBox extends HBox {

    private final Button preBtn;
    private final Button nextBtn;
    private final Label countLbl;
    private final TextField searchField;
    private final CodeEditor codeEditor;
    private Consumer<SearchBox> closeCallback;
    private volatile SearchResult searchResult;

    public SearchBox(CodeEditor codeEditor) {
        this.codeEditor = codeEditor;
        setMinHeight(36);
        setMaxHeight(36);
        setAlignment(Pos.CENTER);
        getStyleClass().add("search-box");

        HBox leftBox = new HBox();
        leftBox.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setPromptText(I18nUtil.t("app.control.code.search"));
        searchField.textProperty().addListener((observableValue, oldVal, newVal) -> searchKeyword(newVal));
        codeEditor.getCodeArea().textProperty().addListener((observableValue, oldVal, newVal) -> {
            if (searchResult != null) {
                searchKeyword(newVal, searchResult.getKeyword());
            }
        });

        leftBox.getChildren().add(searchField);

        HBox rightBox = new HBox();
        rightBox.setAlignment(Pos.CENTER);
        rightBox.setPadding(new Insets(0, 8, 0, 8));

        countLbl = new Label();
        countToZero();
        rightBox.getChildren().add(countLbl);

        preBtn = new Button(IConFontConst.ARROW_UP);
        preBtn.setFont(IConFontConst.FONT);
        preBtn.setOnAction(e -> preResultItem());
        preBtn.setDisable(true);
        nextBtn = new Button(IConFontConst.ARROW_DOWN);
        nextBtn.setFont(IConFontConst.FONT);
        nextBtn.setOnAction(e -> nextResultItem());
        nextBtn.setDisable(true);
        rightBox.getChildren().addAll(preBtn, nextBtn);

        Region region = new Region();
        rightBox.getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        Button closeBtn = new Button(IConFontConst.CLOSE);
        closeBtn.setFont(IConFontConst.FONT);
        closeBtn.setOnAction(e -> {
            if (closeCallback != null) {
                closeCallback.accept(this);
            }
        });
        rightBox.getChildren().add(closeBtn);

        SplitPane splitPane = new SplitPane(leftBox, rightBox);
        splitPane.setDividerPositions(0.3);
        splitPane.setPadding(Insets.EMPTY);
        HBox.setHgrow(splitPane, Priority.ALWAYS);
        getChildren().add(splitPane);
    }

    private void preResultItem() {
        if (searchResult != null && searchResult.getTotal() > 0) {
            SearchResult.SearchItem pre = searchResult.getItem().getPre();
            selectResultItem(pre);
            searchResult.setItem(pre);
        }
    }

    private void nextResultItem() {
        if (searchResult != null && searchResult.getTotal() > 0) {
            SearchResult.SearchItem next = searchResult.getItem().getNext();
            selectResultItem(next);
            searchResult.setItem(next);
        }
    }

    private void selectResultItem(SearchResult.SearchItem searchItem) {
        UIUtil.runUI(() -> {
            int startIndex = searchItem.getStartIndex();
            int endIndex = searchItem.getEndIndex();
            CodeArea codeArea = codeEditor.getCodeArea();
            codeArea.selectRange(startIndex, endIndex);
            TwoDimensional.Position position = codeArea.offsetToPosition(endIndex, TwoDimensional.Bias.Backward);
            codeArea.showParagraphAtCenter(position.getMajor());
            countLbl.setText(searchItem.getNo() + FileNameUtil.SEPARATOR + searchResult.getTotal());
        });
    }

    private void searchKeyword(String keyword) {
        String text = codeEditor.getCodeArea().getText();
        searchKeyword(text, keyword);
    }

    private void searchKeyword(String text, String keyword) {
        long version = System.currentTimeMillis();
        UIUtil.runNotUI(() -> searchKeyword(text, keyword, version));
    }


    private void searchKeyword(String text, String keyword, long version) {
        SearchResult searchResult = new SearchResult();
        searchResult.setVersion(version);
        searchResult.setKeyword(keyword);
        if (StringUtil.isEmpty(text) || StringUtil.isEmpty(keyword)) {
            searchResult.setTotal(0);
            updateSearchResult(searchResult);
            return;
        }
        int total = 0;
        int index = 0;
        SearchResult.SearchItem item = null;
        while (index < text.length() && index >= 0) {
            index = text.indexOf(keyword, index);
            if (index != -1) {
                SearchResult.SearchItem next = new SearchResult.SearchItem();
                next.setNo(++total);
                next.setStartIndex(index);
                next.setEndIndex((index = index + keyword.length()));
                if (item == null) {
                    item = next;
                    item.setPre(item);
                    item.setNext(item);
                } else {
                    SearchResult.SearchItem itemNext = item.getNext();
                    item.setNext(next);
                    next.setPre(item);
                    next.setNext(itemNext);
                    itemNext.setPre(next);
                    item = next;
                }
            }
        }
        searchResult.setTotal(total);
        searchResult.setItem(item == null ? null : item.getNext());
        updateSearchResult(searchResult);
    }

    private void updateSearchResult(SearchResult newResult) {
        SearchResult oldResult = this.searchResult;
        if (oldResult != null && oldResult.getVersion() >= newResult.getVersion()) {
            return;
        }
        synchronized (this) {
            if (searchResult != null && searchResult.getVersion() >= newResult.getVersion()) {
                return;
            }
            searchResult = newResult;
            if (searchResult.getTotal() > 0) {
                SearchResult.SearchItem toItem = searchResult.getItem();
                if (searchResult.getTotal() > 1) {
                    int caretPosition = codeEditor.getCodeArea().getCaretPosition();
                    for (int i = 0; i < searchResult.getTotal(); ++i) {
                        if (toItem.getStartIndex() >= caretPosition) {
                            break;
                        }
                        toItem = toItem.getNext();
                    }
                }
                selectResultItem(toItem);
                changeBtnStatus(false);
            } else {
                countToZero();
                changeBtnStatus(true);
            }
        }
    }

    private void changeBtnStatus(boolean isDisable) {
        UIUtil.runUI(() -> {
            preBtn.setDisable(isDisable);
            nextBtn.setDisable(isDisable);
        });
    }

    private void countToZero() {
        UIUtil.runUI(() -> countLbl.setText(0 + I18nUtil.t("app.control.code.search-result")));
    }

    public void setCloseCallback(Consumer<SearchBox> closeCallback) {
        this.closeCallback = closeCallback;
    }

    public void start() {
        String selectedText = codeEditor.getCodeArea().getSelectedText();
        if (!StringUtil.isEmpty(selectedText) && (searchResult == null || !selectedText.equals(searchResult.getKeyword()))) {
            searchField.setText(selectedText);
            searchField.end();
        }
        searchField.requestFocus();
    }

    public void end() {
        searchResult = null;
        searchField.clear();
        countToZero();
    }
}
