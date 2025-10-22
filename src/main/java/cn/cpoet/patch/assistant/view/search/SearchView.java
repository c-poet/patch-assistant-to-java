package cn.cpoet.patch.assistant.view.search;

import cn.cpoet.patch.assistant.control.DialogPurePane;
import cn.cpoet.patch.assistant.control.tree.AppTreeInfo;
import cn.cpoet.patch.assistant.control.tree.PatchTreeInfo;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.SearchConf;
import cn.cpoet.patch.assistant.core.SearchItem;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.view.home.HomeContext;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * 搜索视图
 *
 * @author CPoet
 */
public class SearchView {

    private Dialog<?> dialog;
    private TextField searchField;
    private final HomeContext context;
    private ListView<SearchItem> searchList;

    public SearchView(HomeContext context) {
        this.context = context;
    }

    public Node build() {
        VBox box = new VBox();
        box.setPadding(Insets.EMPTY);
        searchField = new TextField();
        searchField.textProperty().addListener((observableValue, oldVal, newVal) -> searchNode(newVal));
        box.getChildren().add(searchField);
        searchList = new ListView<>();
        searchList.setCellFactory(l -> new SearchListCell(this));
        VBox.setVgrow(searchList, Priority.ALWAYS);
        box.getChildren().add(searchList);
        searchHistory();
        return box;
    }

    private Stack<String> createPathStack(boolean isPatch) {
        Stack<String> stack = new Stack<>();
        stack.push(isPatch ? "R:" : "L:");
        return stack;
    }

    private void searchHistory() {
        SearchConf search = Configuration.getInstance().getSearch();
        Set<SearchItem> history = search.getHistory();
        if (history != null) {
            searchList.getItems().addAll(history);
        }
    }

    private void searchNode(String keyword) {
        searchList.getItems().clear();
        if (StringUtil.isBlank(keyword)) {
            searchHistory();
            return;
        }
        String[] paths = keyword.split("[/\\\\]");
        AppTreeInfo appTreeInfo = context.getAppTree().getTreeInfo();
        PatchTreeInfo patchTreeInfo = context.getPatchTree().getTreeInfo();
        if (paths.length > 1) {
            SearchKeyword[] searchKeywords = createKeywords(paths);
            if (appTreeInfo != null) {
                searchNodeWithPath(appTreeInfo.getRootNode(), searchKeywords);
            }
            if (patchTreeInfo != null) {
                searchNodeWithPath(patchTreeInfo.getRootNode(), searchKeywords);
            }
        } else {
            SearchKeyword searchKeyword = createKeyword(keyword);
            if (appTreeInfo != null) {
                searchNode(appTreeInfo.getRootNode(), searchKeyword);
            }
            if (patchTreeInfo != null) {
                searchNode(patchTreeInfo.getRootNode(), searchKeyword);
            }
        }
    }

    private void searchNode(TreeNode node, SearchKeyword keyword) {
        if (node.getChildren() != null) {
            node.getChildren().forEach(child -> searchNode(child, keyword, createPathStack(node.isPatch())));
        }
    }

    private void searchNode(TreeNode node, SearchKeyword keyword, Stack<String> paths) {
        if (matchKeyword(keyword, node.getName())) {
            SearchNodeItem item = new SearchNodeItem();
            item.setName(node.getName());
            item.setNode(node);
            item.setPath(String.join("/", paths));
            searchList.getItems().add(item);
        }
        if (node.getChildren() != null) {
            paths.push(node.getName());
            node.getChildren().forEach(child -> searchNode(child, keyword, paths));
            paths.pop();
        }
    }

    private void searchNodeWithPath(TreeNode node, SearchKeyword[] keywords) {
        if (node.getChildren() != null) {
            node.getChildren().forEach(child -> searchNodeWithPath(node, keywords, createPathStack(node.isPatch())));
        }
    }

    private void searchNodeWithPath(TreeNode node, SearchKeyword[] keywords, Stack<String> pathStack) {
        searchNodeWithPath(node, keywords, 0, pathStack);
    }

    private void searchNodeWithPath(TreeNode node, SearchKeyword[] keywords, int index, Stack<String> paths) {
        if (index < keywords.length) {
            if (matchKeyword(keywords[index], node.getName())) {
                if (index == keywords.length - 1) {
                    SearchNodeItem item = new SearchNodeItem();
                    item.setName(node.getName());
                    item.setNode(node);
                    item.setPath(String.join("/", paths));
                    searchList.getItems().add(item);
                } else if (node.getChildren() != null) {
                    paths.push(node.getName());
                    node.getChildren().forEach(child -> searchNodeWithPath(child, keywords, index + 1, paths));
                    paths.pop();
                }
            }
        }
        if (node.getChildren() != null) {
            paths.push(node.getName());
            node.getChildren().forEach(child -> searchNodeWithPath(child, keywords, paths));
            paths.pop();
        }
    }

    private boolean matchKeyword(SearchKeyword keyword, String name) {
        if (keyword.getPattern() != null) {
            return keyword.getPattern().matcher(name).matches();
        }
        return name.contains(keyword.getKeyword());
    }

    private SearchKeyword[] createKeywords(String[] keywords) {
        SearchKeyword[] searchKeywords = new SearchKeyword[keywords.length];
        for (int i = 0; i < keywords.length; ++i) {
            searchKeywords[i] = createKeyword(keywords[i]);
        }
        return searchKeywords;
    }

    private SearchKeyword createKeyword(String keyword) {
        SearchKeyword searchKeyword = new SearchKeyword();
        searchKeyword.setKeyword(keyword);
        if (keyword.indexOf('*') != -1) {
            Pattern pattern = Pattern.compile(keyword.replace("*", ".*"));
            searchKeyword.setPattern(pattern);
        }
        return searchKeyword;
    }

    public void showDialog(Stage stage) {
        dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        dialog.setTitle("搜索");
        DialogPane dialogPane = new DialogPurePane();
        dialogPane.setContent(build());
        Configuration configuration = Configuration.getInstance();
        dialogPane.setPrefSize(configuration.getSearchWidth(), configuration.getSearchHeight());
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setSearchWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setSearchHeight(newVal.doubleValue()));
        dialog.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public void close() {
        dialog.close();
    }

    private static class SearchListCell extends ListCell<SearchItem> {

        public SearchListCell(SearchView view) {
            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    SearchItem item = getItem();
                    if (e.getClickCount() == 2) {
                        if (item instanceof SearchNodeItem) {
                            TreeNode node = ((SearchNodeItem) item).getNode();
                            TreeView<TreeNode> treeView = node.isPatch() ? view.context.getPatchTree() : view.context.getAppTree();
                            treeView.getSelectionModel().clearSelection();
                            treeView.getSelectionModel().select(node.getTreeItem());
                            int row = treeView.getRow(node.getTreeItem());
                            treeView.scrollTo(row);
                            SearchItem historyItem = new SearchItem();
                            historyItem.setName(view.searchField.getText());
                            historyItem.setPath("Search History");
                            Configuration.getInstance().getSearch().addHistory(historyItem);
                            view.close();
                        }
                    } else if (e.getClickCount() == 1) {
                        if (!(item instanceof SearchNodeItem)) {
                            view.searchField.setText(item.getName());
                        }
                    }
                }
            });
        }

        @Override
        protected void updateItem(SearchItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox box = new HBox();
                box.setSpacing(10);
                Label nameLbl = new Label(item.getName());
                box.getChildren().add(nameLbl);
                Label pathLbl = new Label(item.getPath());
                pathLbl.setTextFill(Color.web("#6c707e"));
                box.getChildren().add(pathLbl);
                setGraphic(box);
            }
        }
    }
}
