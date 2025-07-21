package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.I18NEnum;
import cn.cpoet.patch.assistant.constant.ThemeEnum;
import cn.cpoet.patch.assistant.control.IntegerField;
import cn.cpoet.patch.assistant.control.StringMappingConverter;
import cn.cpoet.patch.assistant.core.*;
import cn.cpoet.patch.assistant.util.EncryptUtil;
import cn.cpoet.patch.assistant.util.FXUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
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
            node.setText(I18nUtil.t("app.view.config.command"));
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
            node.setText(I18nUtil.t("app.view.config.directory"));
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
            node.setText(I18nUtil.t("app.view.config.address"));
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
            node.setText(I18nUtil.t("app.view.config.username"));
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
            node.setText(I18nUtil.t("app.view.config.password"));
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
            node.setText(I18nUtil.t("app.view.config.command"));
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
            node.setText(I18nUtil.t("app.view.config.directory"));
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
        dockerConfigPane.setText(I18nUtil.t("app.view.config.docker-config"));
        VBox dockerConfigBox = new VBox();
        dockerConfigBox.setSpacing(10);

        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton localRadioBtn = new RadioButton(I18nUtil.t("app.view.config.docker-local"));
        localRadioBtn.setSelected(!DockerConf.TYPE_REMOTE.equals(docker.getType()));
        localRadioBtn.setToggleGroup(toggleGroup);
        RadioButton remoteRadioBtn = new RadioButton(I18nUtil.t("app.view.config.docker-remote"));
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
        patchConfigPane.setText(I18nUtil.t("app.view.config.patch-config"));
        VBox patchConfigBox = new VBox();
        patchConfigBox.setSpacing(10);

        patchConfigBox.getChildren().add(FXUtil.pre(new HBox(new Label(I18nUtil.t("app.view.config.patch-username")), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(patch.getUsernameOrEnv());
            node.textProperty().addListener((e, oldVal, newVal) -> {
                patch.setUsername(newVal);
            });
        })), box -> box.setAlignment(Pos.CENTER)));

        patchConfigBox.getChildren().add(FXUtil.pre(new HBox(
                FXUtil.pre(new RadioButton(I18nUtil.t("app.view.config.patch-path-match")), node -> {
                    node.setSelected(Boolean.TRUE.equals(patch.getPathMatch()));
                    node.setOnAction(e -> patch.setPathMatch(!Boolean.TRUE.equals(patch.getPathMatch())));
                }),
                FXUtil.pre(new RadioButton(I18nUtil.t("app.view.config.patch-name-match")), node -> {
                    node.setSelected(Boolean.TRUE.equals(patch.getFileNameMatch()));
                    node.setOnAction(e -> patch.setFileNameMatch(!Boolean.TRUE.equals(patch.getFileNameMatch())));
                })
        ), box -> {
            box.setAlignment(Pos.CENTER_LEFT);
            box.setSpacing(10);
        }));

        patchConfigBox.getChildren().add(FXUtil.pre(new HBox(new Label(I18nUtil.t("app.view.config.readme-file")), FXUtil.pre(new TextField(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setText(patch.getReadmeFile());
            node.textProperty().addListener((observableValue, oldVal, newVal) -> {
                patch.setReadmeFile(newVal);
            });
        })), box -> box.setAlignment(Pos.CENTER)));

        patchConfigBox.getChildren().add(FXUtil.pre(new RadioButton(I18nUtil.t("app.view.config.write-patch-sign")), node -> {
            node.setSelected(Boolean.TRUE.equals(patch.getWritePatchSign()));
            node.setOnAction(e -> patch.setWritePatchSign(!Boolean.TRUE.equals(patch.getWritePatchSign())));
        }));

        patchConfigPane.setContent(patchConfigBox);
        return patchConfigPane;
    }

    private Node buildSearchConfig() {
        TitledPane searchConfigPane = new TitledPane();
        searchConfigPane.setCollapsible(false);
        searchConfigPane.setText(I18nUtil.t("app.view.config.search-config"));
        VBox searchConfigBox = new VBox();
        searchConfigBox.getChildren().add(FXUtil.pre(new HBox(new Label(I18nUtil.t("app.view.config.search-save-history-limit")), FXUtil.pre(new IntegerField(), node -> {
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
        generaConfigPane.setText(I18nUtil.t("app.view.config.genera-config"));
        VBox generaConfigBox = new VBox();
        generaConfigBox.setSpacing(10);

        HBox langConfig = new HBox(new Label(I18nUtil.t("app.view.config.genera-lang")), FXUtil.pre(new ComboBox<I18NEnum>(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.getItems().addAll(I18NEnum.values());
            node.setConverter(new StringMappingConverter<>(lang -> I18nUtil.t("app.view.config.genera-lang." + lang.getCode())));
            node.valueProperty().addListener((e, oldVal, newVal) -> genera.setLanguage(newVal.getCode()));
            node.setValue(I18NEnum.ofCode(genera.getLanguage()));
        }));
        langConfig.setAlignment(Pos.CENTER_LEFT);

        HBox themeConfig = new HBox(new Label(I18nUtil.t("app.view.config.genera-theme")), FXUtil.pre(new ComboBox<ThemeEnum>(), node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            node.getItems().addAll(ThemeEnum.values());
            node.setConverter(new StringMappingConverter<>(theme -> I18nUtil.t("app.view.config.genera-theme." + theme.getCode())));
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
        configViewDialog.setTitle(I18nUtil.t("app.view.config.title"));
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
