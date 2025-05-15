package cn.cpoet.patch.assistant.controller;

import cn.cpoet.patch.assistant.ViewLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class HomeController {


    public static Scene getView() throws IOException {
        return ViewLoader.load2Scene("home-view.fxml", 720, 560);
    }
}