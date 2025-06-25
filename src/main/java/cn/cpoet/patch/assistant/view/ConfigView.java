package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.I18NEnum;
import cn.cpoet.patch.assistant.constant.ThemeEnum;
import cn.cpoet.patch.assistant.control.IntegerField;
import cn.cpoet.patch.assistant.core.*;
import cn.cpoet.patch.assistant.util.EncryptUtil;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
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

    private final PatchConf patch;
    private final SearchConf search;
    private final GeneraConf genera;
    private final DockerConf docker;

    public ConfigView() {
        Configuration configuration = Configuration.getInstance();
        patch = configuration.getPatch().clone();
        genera = configuration.getGenera().clone();
        docker = configuration.getDocker().clone();
        search = configuration.getSearch().clone();
    }

    private Node buildDockerLocalConfig() {
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

    private Node buildDockerRemoteConfig() {
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
        }), FXUtil.pre(new IntegerField(), node -> {
            node.setNumber(docker.getPort());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                docker.setPort(node.getNumber());
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
        }), FXUtil.pre(new PasswordField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (!StringUtil.isBlank(docker.getPassword())) {
                String pass = EncryptUtil.decryptWithRsaSys(docker.getPassword());
                node.setText(pass);
            }
            node.textProperty().addListener((e, oldVal, newVal) -> {
                if (StringUtil.isBlank(newVal)) {
                    docker.setPassword(null);
                } else {
                    String s = EncryptUtil.encryptWithRsaSys(newVal);
                    docker.setPassword(s);
                }
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

    private Node buildDockerConfig() {
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

    private void swapDockerConfig(Pane pane) {
        Node node = DockerConf.TYPE_REMOTE.equals(docker.getType()) ? buildDockerRemoteConfig() : buildDockerLocalConfig();
        if (pane.getChildren().size() > 1) {
            pane.getChildren().set(1, node);
        } else {
            pane.getChildren().add(1, node);
        }
    }

    private Node buildPatchConfig() {
        TitledPane patchConfigPane = new TitledPane();
        patchConfigPane.setCollapsible(false);
        patchConfigPane.setText("补丁配置");
        VBox patchConfigBox = new VBox();
        patchConfigBox.setSpacing(10);

        patchConfigBox.getChildren().add(FXUtil.pre(new HBox(
                FXUtil.pre(new RadioButton("开启路径匹配"), node -> {
                    node.setSelected(Boolean.TRUE.equals(patch.getPathMatch()));
                    node.setOnAction(e -> patch.setPathMatch(!Boolean.TRUE.equals(patch.getPathMatch())));
                }),
                FXUtil.pre(new RadioButton("开启文件名匹配"), node -> {
                    node.setSelected(Boolean.TRUE.equals(patch.getFileNameMatch()));
                    node.setOnAction(e -> patch.setFileNameMatch(!Boolean.TRUE.equals(patch.getFileNameMatch())));
                })
        ), box -> {
            box.setAlignment(Pos.CENTER_LEFT);
            box.setSpacing(10);
        }));

        patchConfigBox.getChildren().add(FXUtil.pre(new HBox(new Label("说明文件: "), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(patch.getReadmeFile());
            node.textProperty().addListener((observableValue, oldVal, newVal) -> {
                patch.setReadmeFile(newVal);
            });
        })), box -> box.setAlignment(Pos.CENTER)));

        patchConfigBox.getChildren().add(FXUtil.pre(new RadioButton("写入补丁签名"), node -> {
            node.setSelected(Boolean.TRUE.equals(patch.getWritePatchSign()));
            node.setOnAction(e -> patch.setWritePatchSign(!Boolean.TRUE.equals(patch.getWritePatchSign())));
        }));

        patchConfigPane.setContent(patchConfigBox);
        return patchConfigPane;
    }

    private Node buildSearchConfig() {
        TitledPane searchConfigPane = new TitledPane();
        searchConfigPane.setCollapsible(false);
        searchConfigPane.setText("搜索配置");
        VBox searchConfigBox = new VBox();
        searchConfigBox.getChildren().add(FXUtil.pre(new HBox(new Label("保留历史条数: "), FXUtil.pre(new IntegerField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setNumber(search.getHistoryLimit());
            node.textProperty().addListener((observableValue, oldVal, newVal) -> {
                search.setHistoryLimit(node.getNumber());
            });
        })), box -> box.setAlignment(Pos.CENTER)));
        searchConfigPane.setContent(searchConfigBox);
        return searchConfigPane;
    }

    private Node buildGeneraConfig() {
        TitledPane generaConfigPane = new TitledPane();
        generaConfigPane.setCollapsible(false);
        generaConfigPane.setText("常规配置");
        VBox generaConfigBox = new VBox();
        generaConfigBox.setSpacing(10);

        HBox langConfig = new HBox(new Label("语言: "), FXUtil.pre(new ComboBox<I18NEnum>(), node -> {
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
            node.valueProperty().addListener((e, oldVal, newVal) -> genera.setLanguage(newVal.getCode()));
            node.setValue(I18NEnum.ofCode(genera.getLanguage()));
        }));
        langConfig.setAlignment(Pos.CENTER_LEFT);

        HBox themeConfig = new HBox(new Label("主题: "), FXUtil.pre(new ComboBox<ThemeEnum>(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.getItems().addAll(ThemeEnum.values());
            node.setConverter(new StringConverter<>() {
                @Override
                public String toString(ThemeEnum themeEnum) {
                    return themeEnum == null ? null : themeEnum.getName();
                }

                @Override
                public ThemeEnum fromString(String name) {
                    return ThemeEnum.ofName(name);
                }
            });
            node.valueProperty().addListener((e, oldVal, newVal) -> genera.setTheme(newVal.getCode()));
            node.setValue(ThemeEnum.ofCode(genera.getTheme()));
        }));
        themeConfig.setAlignment(Pos.CENTER_LEFT);
        generaConfigBox.getChildren().add(FXUtil.pre(new HBox(themeConfig, langConfig), node -> node.setSpacing(15)));

        generaConfigPane.setContent(generaConfigBox);
        return generaConfigPane;
    }

    public Node build() {
        VBox configBox = new VBox();
        configBox.setSpacing(5);
        configBox.setPadding(Insets.EMPTY);
        configBox.getChildren().add(buildGeneraConfig());
        configBox.getChildren().add(buildSearchConfig());
        configBox.getChildren().add(buildPatchConfig());
        configBox.getChildren().add(buildDockerConfig());
        ScrollPane scrollPane = new ScrollPane(configBox);
        scrollPane.setPadding(Insets.EMPTY);
        scrollPane.setFitToWidth(true);
        return scrollPane;
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
            AppContext appContext = AppContext.getInstance();
            ThemeEnum theme = appContext.curTheme();
            configuration.setGenera(genera);
            configuration.setDocker(docker);
            configuration.setSearch(search);
            configuration.setPatch(patch);
            appContext.syncConf2File();
            if (!genera.getTheme().equals(theme.getCode())) {
                appContext.updateTheme();
            }
        }
    }
}
