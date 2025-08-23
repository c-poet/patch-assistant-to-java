package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.constant.IConConst;
import cn.cpoet.patch.assistant.constant.StyleConst;
import cn.cpoet.patch.assistant.control.tree.AppTreeView;
import cn.cpoet.patch.assistant.control.tree.PatchRootInfo;
import cn.cpoet.patch.assistant.control.tree.PatchTreeInfo;
import cn.cpoet.patch.assistant.control.tree.PatchTreeView;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.AppPackService;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.about.AboutView;
import cn.cpoet.patch.assistant.view.config.ConfigView;
import cn.cpoet.patch.assistant.view.progress.ProgressView;
import cn.cpoet.patch.assistant.view.search.SearchView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import java.util.Map;
import java.util.Set;

public class HomeView extends HomeContext {

    private final Stage stage;
    private final BooleanProperty showPatchInfo;

    public HomeView(Stage stage) {
        this.stage = stage;
        showPatchInfo = new SimpleBooleanProperty(Boolean.TRUE.equals(Configuration.getInstance().getShowPatchInfo()));
    }

    private Node buildHeader() {

        Configuration configuration = Configuration.getInstance();

        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        CheckBox showPatchInfoCB = new CheckBox(I18nUtil.t("app.view.home.show-patch-info"));
        showPatchInfoCB.setSelected(showPatchInfo.get());
        showPatchInfoCB.setOnAction(e -> showPatchInfo.set(!showPatchInfo.get()));
        headerBox.getChildren().add(showPatchInfoCB);

        CheckBox checkSelectedLink = new CheckBox(I18nUtil.t("app.view.home.select-linkage"));
        checkSelectedLink.setSelected(Boolean.TRUE.equals(configuration.getIsSelectedLinked()));
        checkSelectedLink.setOnAction(e -> configuration.setIsSelectedLinked(!Boolean.TRUE.equals(configuration.getIsSelectedLinked())));
        headerBox.getChildren().add(checkSelectedLink);

        CheckBox showFileDetail = new CheckBox(I18nUtil.t("app.view.home.file-detail"));
        showFileDetail.setSelected(Boolean.TRUE.equals(configuration.getIsShowFileDetail()));
        showFileDetail.setOnAction(e -> {
            configuration.setIsShowFileDetail(!Boolean.TRUE.equals(configuration.getIsShowFileDetail()));
            appTree.refresh();
            patchTree.refresh();
        });
        headerBox.getChildren().add(showFileDetail);

        CheckBox onlyChanges = new CheckBox(I18nUtil.t("app.view.home.only-show-change"));
        onlyChanges.setSelected(Boolean.TRUE.equals(configuration.getIsOnlyChanges()));
        onlyChanges.setOnAction(e -> {
            configuration.setIsOnlyChanges(!Boolean.TRUE.equals(configuration.getIsOnlyChanges()));
            appTree.fireEvent(new Event(AppTreeView.APP_TREE_NONE_REFRESH_CALL));
        });
        headerBox.getChildren().add(onlyChanges);

        CheckBox checkDockerImage = new CheckBox(I18nUtil.t("app.view.home.docker-image"));
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

        Button configBtn = new Button(I18nUtil.t("app.view.home.config"));
        configBtn.setOnAction(e -> new ConfigView().showDialog(stage));
        headerBox.getChildren().add(configBtn);

        Button aboutBtn = new Button(I18nUtil.t("app.view.home.about"));
        aboutBtn.setOnAction(e -> new AboutView().showDialog(stage));
        headerBox.getChildren().add(aboutBtn);
        headerBox.setPadding(new Insets(3, 8, 3, 8));
        headerBox.setAlignment(Pos.CENTER);

        TitledPane titledPane = new TitledPane(I18nUtil.t("app.view.home.option"), headerBox);
        titledPane.setCollapsible(false);
        return titledPane;
    }

    private void updateReadmeText(TextArea readMeTextArea) {
        PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
        readMeTextArea.setText(patchTreeInfo.getReadMeText());
    }

    private Node buildBottomCentre() {
        TextArea readMeTextArea = new TextArea();
        readMeTextArea.setEditable(false);
        patchTree.addEventHandler(PatchTreeView.PATCH_TREE_REFRESH, e -> updateReadmeText(readMeTextArea));
        patchTree.addEventHandler(PatchTreeView.PATCH_MARK_ROOT_CHANGE, e -> updateReadmeText(readMeTextArea));
        PatchTreeInfo patchTreeInfo = patchTree.getTreeInfo();
        if (patchTreeInfo != null) {
            readMeTextArea.setText(patchTreeInfo.getReadMeText());
        }
        TitledPane titledPane = new TitledPane(I18nUtil.t("app.view.home.patch-info"), readMeTextArea);
        titledPane.setCollapsible(false);
        return titledPane;
    }

    private void focusTree(SplitPane pane, Node leftTree, Node rightTree, int status) {
        pane.getItems().clear();
        if (status == 0) {
            pane.getItems().addAll(leftTree, rightTree);
        } else if (status == 1) {
            pane.getItems().add(leftTree);
        } else {
            pane.getItems().add(rightTree);
        }
    }

    private void handleShowPatchInfo(SplitPane centrePane, Node top, Node bottom, boolean showPatchInfo) {
        centrePane.getItems().clear();
        centrePane.getItems().add(top);
        if (showPatchInfo) {
            centrePane.getItems().add(bottom);
        }
    }

    private Node buildCentre() {
        StackPane treeStackPane = new StackPane();
        Node leftTree = new HomeLeftTreeView(stage, this).build();
        Node rightTree = new HomeRightTreeView(stage, this).build();
        SplitPane topPane = new SplitPane();
        focusTree(topPane, leftTree, rightTree, focusTreeStatus.get());
        focusTreeStatus.addListener((observableValue, oldVal, newVal) -> {
            int focusStatus = newVal.intValue();
            Configuration.getInstance().setFocusTreeStatus(focusStatus);
            focusTree(topPane, leftTree, rightTree, focusStatus);
        });
        treeStackPane.getChildren().add(topPane);
        Node patchInfo = buildBottomCentre();
        SplitPane centrePane = new SplitPane(treeStackPane, patchInfo);
        handleShowPatchInfo(centrePane, treeStackPane, patchInfo, showPatchInfo.get());
        showPatchInfo.addListener((observableValue, oldVal, newVal) -> {
            Configuration.getInstance().setShowPatchInfo(newVal);
            handleShowPatchInfo(centrePane, treeStackPane, patchInfo, newVal);
        });
        centrePane.setOrientation(Orientation.VERTICAL);
        centrePane.setDividerPositions(0.7);
        return centrePane;
    }

    private boolean hasRepeatPatchWithSha1() {
        Set<String> allPatchUpSignSha1 = appTree.getTreeInfo().getAllPatchUpSignSha1();
        if (CollectionUtil.isEmpty(allPatchUpSignSha1)) {
            return false;
        }
        PatchTreeInfo treeInfo = patchTree.getTreeInfo();
        if (allPatchUpSignSha1.contains(treeInfo.getRootInfo().getPatchSign().getSha1())) {
            return true;
        }
        if (CollectionUtil.isNotEmpty(treeInfo.getCustomRootInfoMap())) {
            for (Map.Entry<TreeNode, PatchRootInfo> entry : treeInfo.getCustomRootInfoMap().entrySet()) {
                if (allPatchUpSignSha1.contains(entry.getValue().getPatchSign().getSha1())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleSaveAppPack(ActionEvent event) {
        if (!totalInfo.isChangeNode()) {
            ButtonType buttonType = AlterUtil.warn(stage, I18nUtil.t("app.view.home.app-not-changed-tip"), ButtonType.YES, ButtonType.NO);
            if (ButtonType.NO.equals(buttonType)) {
                return;
            }
        } else {
            if (hasRepeatPatchWithSha1()) {
                ButtonType buttonType = AlterUtil.warn(stage, I18nUtil.t("app.view.home.patch-duplication-tip"), ButtonType.YES, ButtonType.NO);
                if (ButtonType.NO.equals(buttonType)) {
                    return;
                }
            }
        }
        // 判断是否Docker模式
        Configuration configuration = Configuration.getInstance();
        boolean isDockerImage = Boolean.TRUE.equals(configuration.getIsDockerImage());
        FileChooser fileChooser = new FileChooser();
        String lastSavePackPath = configuration.getLastSavePackPath();
        if (!StringUtil.isBlank(lastSavePackPath)) {
            fileChooser.setInitialDirectory(FileUtil.getExistsDirOrFile(lastSavePackPath));
        }
        String fileName = appTree.getTreeInfo().getRootNode().getName();
        if (isDockerImage) {
            fileChooser.setTitle(I18nUtil.t("app.view.home.save-docker-image"));
            fileChooser.setInitialFileName(FileNameUtil.getName(fileName) + ".tar");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18nUtil.t("app.view.home.docker-image"), "*.tar"));
        } else {
            fileChooser.setTitle(I18nUtil.t("app.view.home.save-jar"));
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18nUtil.t("app.view.home.java-package"), "*.jar"));
        }
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        configuration.setLastSavePackPath(file.getParent());
        new ProgressView(fileChooser.getTitle()).showDialog(stage, (progressContext)
                -> AppPackService.INSTANCE.savePack(this, progressContext, file, isDockerImage));
    }

    private Node buildFooter() {
        HBox footerBox = new HBox(
                FXUtil.pre(new Label(I18nUtil.t("app.view.home.patch-add")), StyleConst.FONT_BOLD),
                FXUtil.pre(new Label(), lbl -> lbl.textProperty().bind(totalInfo.addTotalProperty().asString())),
                FXUtil.pre(new Label(I18nUtil.t("app.view.home.patch-mod")), StyleConst.FONT_BOLD),
                FXUtil.pre(new Label(), lbl -> lbl.textProperty().bind(totalInfo.modTotalProperty().asString())),
                FXUtil.pre(new Label(I18nUtil.t("app.view.home.patch-del")), StyleConst.FONT_BOLD),
                FXUtil.pre(new Label(), lbl -> lbl.textProperty().bind(totalInfo.delTotalProperty().asString())),
                FXUtil.pre(new Label(I18nUtil.t("app.view.home.patch-manual-del")), StyleConst.FONT_BOLD),
                FXUtil.pre(new Label(), lbl -> lbl.textProperty().bind(totalInfo.manualDelTotalProperty().asString())),
                FXUtil.pre(new Region(), node -> HBox.setHgrow(node, Priority.ALWAYS)),
                FXUtil.pre(new Button(), btn -> {
                    btn.setDisable(appTree.getRoot() == null);
                    appTree.addEventHandler(AppTreeView.APP_TREE_REFRESH, e -> btn.setDisable(appTree.getRoot() == null));
                    btn.setText(I18nUtil.t("app.view.home.save"));
                    btn.setOnAction(this::handleSaveAppPack);
                })
        );
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setSpacing(3);
        footerBox.setPadding(new Insets(5, 3, 0, 3));
        return footerBox;
    }

    private void showSearchView() {
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