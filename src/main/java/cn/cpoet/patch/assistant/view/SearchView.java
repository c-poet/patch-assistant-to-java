package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.control.DialogPurePane;
import cn.cpoet.patch.assistant.view.tree.TreeNode;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 搜索视图
 *
 * @author CPoet
 */
public class SearchView {

    protected Dialog<?> dialog;
    protected TextField searchField;
    protected final HomeContext context;
    protected ListView<TreeNode> searchList;

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
        return box;
    }

    protected void searchNode(String keyword) {
        searchList.getItems().clear();
        searchNode(context.appTreeInfo.getRootNode(), keyword);
        searchNode(context.patchTreeInfo.getRootNode(), keyword);
    }

    protected void searchNode(TreeNode node, String keyword) {
        if (node == null) {
            return;
        }
        if (node.getName().contains(keyword)) {
            searchList.getItems().add(node);
        }
        if (node.getChildren() != null) {
            node.getChildren().forEach(child -> searchNode(child, keyword));
        }
    }

    public void showDialog(Stage stage) {
        dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        dialog.setTitle("搜索");
        DialogPane dialogPane = new DialogPurePane();
        dialogPane.setContent(build());
        dialogPane.setPrefSize(720, 300);
        dialog.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public void close() {
        dialog.close();
    }

    protected static class SearchListCell extends ListCell<TreeNode> {

        public SearchListCell(SearchView view) {
            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    TreeNode node = getItem();
                    if (node.isPatch()) {
                        view.context.patchTree.getSelectionModel().select(node.getTreeItem());
                    } else {
                        view.context.appTree.getSelectionModel().select(node.getTreeItem());
                    }
                    view.close();
                }
            });
        }

        @Override
        protected void updateItem(TreeNode node, boolean empty) {
            super.updateItem(node, empty);
            if (empty || node == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(node.getName());
            }
        }
    }
}
