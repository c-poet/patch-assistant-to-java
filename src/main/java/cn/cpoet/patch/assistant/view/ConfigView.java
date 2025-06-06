package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.I18NEnum;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.DockerConf;
import cn.cpoet.patch.assistant.core.GeneraConf;
import cn.cpoet.patch.assistant.util.FXUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * 配置界面
 *
 * @author CPoet
 */
public class ConfigView {

    private final GeneraConf genera;
    private final DockerConf docker;

    public ConfigView() {
        Configuration configuration = Configuration.getInstance();
        genera = configuration.getGenera().clone();
        docker = configuration.getDocker().clone();
    }

    protected Node buildDockerLocalConfig() {
        VBox box = new VBox();
        box.setSpacing(5);
        HBox commandConfig = new HBox(FXUtil.pre(new Label(), node -> {
            node.setText("命 令: ");
        }), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(docker.getLocalCommand());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setLocalCommand(newVal);
            });
        }));
        commandConfig.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(commandConfig);
        HBox workPathConfig = new HBox(FXUtil.pre(new Label(), node -> {
            node.setText("目 录: ");
        }), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(docker.getLocalWorkPath());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setLocalWorkPath(newVal);
            });
        }));
        workPathConfig.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(workPathConfig);
        return box;
    }

    protected Node buildDockerRemoteConfig() {
        VBox box = new VBox();
        box.setSpacing(5);
        HBox hostConfig = new HBox(FXUtil.pre(new Label(), node -> {
            node.setText("地 址: ");
        }), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(docker.getHost());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setHost(newVal);
            });
        }), FXUtil.pre(new Label(), node -> {
            node.setText(" : ");
        }), FXUtil.pre(new TextField(), node -> {
            node.setText(docker.getPort());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setPort(newVal);
            });
        }));
        hostConfig.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(hostConfig);

        HBox usernameConfig = new HBox(FXUtil.pre(new Label(), node -> {
            node.setText("用 户: ");
        }), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(docker.getUsername());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setUsername(newVal);
            });
        }));
        usernameConfig.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(usernameConfig);
        HBox passwordConfig = new HBox(FXUtil.pre(new Label(), node -> {
            node.setText("密 码: ");
        }), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(docker.getPassword());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setPassword(newVal);
            });
        }));
        passwordConfig.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(passwordConfig);
        HBox commandConfig = new HBox(FXUtil.pre(new Label(), node -> {
            node.setText("命 令: ");
        }), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(docker.getCommand());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setCommand(newVal);
            });
        }));
        commandConfig.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(commandConfig);
        HBox workPathConfig = new HBox(FXUtil.pre(new Label(), node -> {
            node.setText("目 录: ");
        }), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(docker.getWorkPath());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setWorkPath(newVal);
            });
        }));
        workPathConfig.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(workPathConfig);
        return box;
    }

    protected Node buildDockerConfig() {
        TitledPane dockerConfigPane = new TitledPane();
        dockerConfigPane.setCollapsible(false);
        dockerConfigPane.setText("Docker配置");
        VBox dockerConfigBox = new VBox();
        dockerConfigBox.setSpacing(10);

        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton localRadioBtn = new RadioButton("本机");
        localRadioBtn.setSelected(!DockerConf.TYPE_REMOTE.equals(docker.getType()));
        localRadioBtn.setToggleGroup(toggleGroup);
        RadioButton remoteRadioBtn = new RadioButton("远程");
        remoteRadioBtn.setSelected(!localRadioBtn.isSelected());
        remoteRadioBtn.setToggleGroup(toggleGroup);
        localRadioBtn.setOnAction(e -> {
            docker.setType(DockerConf.TYPE_LOCAL);
            swapDockerConfig(dockerConfigBox);
        });
        remoteRadioBtn.setOnAction(e -> {
            docker.setType(DockerConf.TYPE_REMOTE);
            swapDockerConfig(dockerConfigBox);
        });
        dockerConfigBox.getChildren().add(FXUtil.pre(new HBox(localRadioBtn, remoteRadioBtn), node -> {
            node.setSpacing(10);
        }));
        dockerConfigPane.setContent(dockerConfigBox);
        swapDockerConfig(dockerConfigBox);
        return dockerConfigPane;
    }

    protected void swapDockerConfig(Pane pane) {
        Node node = DockerConf.TYPE_REMOTE.equals(docker.getType()) ? buildDockerRemoteConfig() : buildDockerLocalConfig();
        if (pane.getChildren().size() > 1) {
            pane.getChildren().set(1, node);
        } else {
            pane.getChildren().add(1, node);
        }
    }

    public Node buildGeneraConfig() {
        TitledPane generaConfigPane = new TitledPane();
        generaConfigPane.setCollapsible(false);
        generaConfigPane.setText("常规配置");
        VBox generaConfigBox = new VBox();
        HBox langConfig = new HBox(FXUtil.pre(new Label(), node -> {
            node.setText("语言: ");
        }), FXUtil.pre(new ComboBox<I18NEnum>(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.getItems().addAll(I18NEnum.values());
            node.setConverter(new StringConverter<>() {
                @Override
                public String toString(I18NEnum i18NEnum) {
                    return i18NEnum == null ? null : i18NEnum.getName();
                }

                @Override
                public I18NEnum fromString(String name) {
                    return I18NEnum.ofName(name);
                }
            });
            node.valueProperty().addListener((e, oldVal, newVal) -> {
                genera.setLanguage(newVal.getCode());
            });
            node.setValue(I18NEnum.ofCode(genera.getLanguage()));
        }));
        langConfig.setAlignment(Pos.CENTER_LEFT);
        generaConfigBox.getChildren().add(langConfig);
        generaConfigPane.setContent(generaConfigBox);
        return generaConfigPane;
    }

    public Node build() {
        VBox configBox = new VBox();
        configBox.setSpacing(5);
        configBox.setPadding(Insets.EMPTY);
        configBox.getChildren().add(buildGeneraConfig());
        configBox.getChildren().add(buildDockerConfig());
        return configBox;
    }

    public void showDialog(Stage stage) {
        Dialog<Boolean> configViewDialog = new Dialog<>();
        configViewDialog.initOwner(stage);
        configViewDialog.initModality(Modality.WINDOW_MODAL);
        configViewDialog.setTitle("配置");
        configViewDialog.setResizable(true);
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(build());
        Configuration configuration = Configuration.getInstance();
        dialogPane.setPrefSize(configuration.getConfigWidth(), configuration.getConfigHeight());
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setConfigWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setConfigHeight(newVal.doubleValue()));
        dialogPane.getButtonTypes().add(ButtonType.OK);
        dialogPane.getButtonTypes().add(ButtonType.CANCEL);
        configViewDialog.setDialogPane(dialogPane);
        configViewDialog.setResultConverter(t -> t == ButtonType.OK);
        if (configViewDialog.showAndWait().orElse(Boolean.FALSE)) {
            configuration.setGenera(genera);
            configuration.setDocker(docker);
            AppContext.getInstance().syncConf2File();
        }
    }
}
