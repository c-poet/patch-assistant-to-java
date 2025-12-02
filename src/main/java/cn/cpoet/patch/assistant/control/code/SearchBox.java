package cn.cpoet.patch.assistant.control.code;

import cn.cpoet.patch.assistant.constant.IConFontConst;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.util.UIUtil;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.fxmisc.richtext.CodeArea;

import java.util.function.Consumer;

/**
 * 搜索框
 *
 * @author CPoet
 */
public class SearchBox extends HBox {

    private final Label countLbl;
    private final TextField searchField;
    private final CodeEditor codeEditor;
    private Consumer<SearchBox> closeCallback;
    private volatile SearchResult searchResult;

    public SearchBox(CodeEditor codeEditor) {
        this.codeEditor = codeEditor;
        setMinHeight(36);
        setMaxHeight(36);
        setSpacing(5);
        setAlignment(Pos.CENTER);
        getStyleClass().add("search-box");

        searchField = new TextField();
        searchField.setPromptText("搜索");
        searchField.textProperty().addListener((observableValue, oldVal, newVal) -> searchKeyword(newVal));
        codeEditor.getCodeArea().textProperty().addListener((observableValue, oldVal, newVal) -> {
            if (searchResult != null) {
                searchKeyword(newVal, searchResult.getKeyword());
            }
        });

        getChildren().add(searchField);
        getChildren().add(new Separator(Orientation.VERTICAL));

        countLbl = new Label();
        countToZero();
        getChildren().add(countLbl);

        Button preBtn = new Button(IConFontConst.ARROW_UP);
        preBtn.setFont(IConFontConst.FONT);
        preBtn.setOnAction(e -> preResultItem());
        Button nextBtn = new Button(IConFontConst.ARROW_DOWN);
        nextBtn.setFont(IConFontConst.FONT);
        nextBtn.setOnAction(e -> nextResultItem());
        getChildren().addAll(preBtn, nextBtn);

        Region region = new Region();
        getChildren().add(region);
        HBox.setHgrow(region, Priority.ALWAYS);

        Button closeBtn = new Button(IConFontConst.CLOSE);
        closeBtn.setFont(IConFontConst.FONT);
        closeBtn.setOnAction(e -> {
            if (closeCallback != null) {
                closeCallback.accept(this);
            }
        });
        getChildren().add(closeBtn);
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
                selectResultItem(searchResult.getItem());
            } else {
                countToZero();
            }
        }
    }

    private void countToZero() {
        UIUtil.runUI(() -> countLbl.setText(0 + "结果"));
    }

    public void setCloseCallback(Consumer<SearchBox> closeCallback) {
        this.closeCallback = closeCallback;
    }

    public void clear() {
        searchResult = null;
        searchField.clear();
        countToZero();
    }
}
