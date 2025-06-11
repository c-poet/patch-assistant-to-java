package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.component.OnlyChangeFilter;
import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.constant.StyleConst;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.AppPackService;
import cn.cpoet.patch.assistant.util.AlterUtil;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.ImageUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class HomeView extends HomeContext {

    private final Stage stage;

    public HomeView(Stage stage) {
        this.stage = stage;
    }

    protected Node buildHeader() {

        Configuration configuration = Configuration.getInstance();

        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        CheckBox checkSelectedLink = new CheckBox("选中联动");
        checkSelectedLink.setSelected(Boolean.TRUE.equals(configuration.getIsSelectedLinked()));
        checkSelectedLink.setOnAction(e -> configuration.setIsSelectedLinked(!Boolean.TRUE.equals(configuration.getIsSelectedLinked())));
        headerBox.getChildren().add(checkSelectedLink);

        CheckBox showFileDetail = new CheckBox("文件详情");
        showFileDetail.setSelected(Boolean.TRUE.equals(configuration.getIsShowFileDetail()));
        showFileDetail.setOnAction(e -> {
            configuration.setIsShowFileDetail(!Boolean.TRUE.equals(configuration.getIsShowFileDetail()));
            appTree.refresh();
            patchTree.refresh();
        });
        headerBox.getChildren().add(showFileDetail);

        CheckBox onlyChanges = new CheckBox("仅看变动");
        onlyChanges.setSelected(Boolean.TRUE.equals(configuration.getIsOnlyChanges()));
        onlyChanges.setOnAction(e -> {
            configuration.setIsOnlyChanges(!Boolean.TRUE.equals(configuration.getIsOnlyChanges()));
            appTree.getRoot().getChildren().clear();
            TreeNodeUtil.buildNode(appTree.getRoot(), appTree.getRoot().getValue(), OnlyChangeFilter.INSTANCE);
            appTree.refresh();
        });
        headerBox.getChildren().add(onlyChanges);

        CheckBox checkDockerImage = new CheckBox("Docker镜像");
        checkDockerImage.setSelected(Boolean.TRUE.equals(configuration.getIsDockerImage()));
        checkDockerImage.setOnAction(e -> configuration.setIsDockerImage(!Boolean.TRUE.equals(configuration.getIsDockerImage())));
        headerBox.getChildren().add(checkDockerImage);

        headerBox.getChildren().add(FXUtil.pre(new Region(), node -> HBox.setHgrow(node, Priority.ALWAYS)));

        Button searchBtn = new Button();
        Image searchImage = ImageUtil.loadImage(IConConst.SEARCH);
        ImageView searchIV = new ImageView(searchImage);
        searchIV.setFitWidth(16);
        searchIV.setFitHeight(16);
        searchBtn.setGraphic(searchIV);
        searchBtn.setOnAction(e -> showSearchView());
        headerBox.getChildren().add(searchBtn);

        Button configBtn = new Button("配置");
        configBtn.setOnAction(e -> new ConfigView().showDialog(stage));
        headerBox.getChildren().add(configBtn);

        Button aboutBtn = new Button("关于");
        aboutBtn.setOnAction(e -> new AboutView().showDialog(stage));
        headerBox.getChildren().add(aboutBtn);
        headerBox.setPadding(new Insets(3, 8, 3, 8));
        headerBox.setAlignment(Pos.CENTER);

        TitledPane titledPane = new TitledPane("选项", headerBox);
        titledPane.setCollapsible(false);
        return titledPane;
    }

    protected Node buildBottomCentre() {
        TextArea readMeTextArea = new TextArea();
        readMeTextArea.setEditable(false);
        readMeTextArea.setText(patchTreeInfo.getReadMeText());
        patchTree.addEventHandler(HomeContext.PATCH_TREE_REFRESH, (EventHandler<Event>) event -> {
            readMeTextArea.setText(patchTreeInfo.getReadMeText());
        });
        if (patchTreeInfo != null) {
            readMeTextArea.setText(patchTreeInfo.getReadMeText());
        }
        TitledPane titledPane = new TitledPane("补丁信息", readMeTextArea);
        titledPane.setCollapsible(false);
        return titledPane;
    }

    protected Node buildCentre() {
        StackPane treeStackPane = new StackPane();
        Node leftTree = new HomeLeftTreeView(stage, this).build();
        Node rightTree = new HomeRightTreeView(stage, this).build();
        SplitPane topPane = new SplitPane(leftTree, rightTree);
        treeStackPane.getChildren().add(topPane);
        SplitPane centrePane = new SplitPane(treeStackPane, buildBottomCentre());
        centrePane.setOrientation(Orientation.VERTICAL);
        centrePane.setDividerPositions(0.7);
        return centrePane;
    }

    protected Node buildFooter() {
        HBox footerBox = new HBox(
                FXUtil.pre(new Label("新增: "), StyleConst.FONT_BOLD),
                FXUtil.pre(new Label(), lbl -> lbl.textProperty().bind(totalInfo.addTotalProperty().asString())),
                FXUtil.pre(new Label("更新: "), StyleConst.FONT_BOLD),
                FXUtil.pre(new Label(), lbl -> lbl.textProperty().bind(totalInfo.modTotalProperty().asString())),
                FXUtil.pre(new Label("删除: "), StyleConst.FONT_BOLD),
                FXUtil.pre(new Label(), lbl -> lbl.textProperty().bind(totalInfo.delTotalProperty().asString())),
                FXUtil.pre(new Label("标记删除: "), StyleConst.FONT_BOLD),
                FXUtil.pre(new Label(), lbl -> lbl.textProperty().bind(totalInfo.markDelTotalProperty().asString())),
                FXUtil.pre(new Region(), node -> HBox.setHgrow(node, Priority.ALWAYS)),
                FXUtil.pre(new Button(), btn -> {
                    btn.setDisable(appTree.getRoot() == null);
                    appTree.addEventHandler(APP_TREE_REFRESH, e -> btn.setDisable(appTree.getRoot() == null));
                    btn.setText("保存");
                    btn.setOnAction(e -> {
                        if (!totalInfo.isChangeNode()) {
                            ButtonType buttonType = AlterUtil.warn(stage, "应用包未改变，是否继续？", ButtonType.YES, ButtonType.NO);
                            if (ButtonType.NO.equals(buttonType)) {
                                return;
                            }
                        }
                        // 判断是否Docker模式
                        boolean isDockerImage = Boolean.TRUE.equals(Configuration.getInstance().getIsDockerImage());
                        FileChooser fileChooser = new FileChooser();
                        if (isDockerImage) {
                            fileChooser.setTitle("保存镜像包");
                            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("应用包", "*.tar"));
                        } else {
                            fileChooser.setTitle("保存应用包");
                            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("应用包", "*.jar"));
                        }
                        File file = fileChooser.showSaveDialog(stage);
                        if (file == null) {
                            return;
                        }
                        new ProgressView(fileChooser.getTitle()).showDialog(stage, (context) -> {
                            AppPackService.getInstance().savePack(context, file, appTreeInfo, isDockerImage);
                        });
                    });
                })
        );
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setSpacing(3);
        footerBox.setPadding(new Insets(5, 3, 0, 3));
        return footerBox;
    }

    protected void showSearchView() {
        new SearchView(this).showDialog(stage);
    }

    public Pane build() {
        BorderPane rootPane = new BorderPane();
        rootPane.setPadding(new Insets(1, 2, 4, 2));
        rootPane.setTop(buildHeader());
        rootPane.setCenter(buildCentre());
        rootPane.setBottom(buildFooter());
        rootPane.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.F) {
                showSearchView();
            }
        });
        return rootPane;
    }
}