package com.dvtrung.sound;

import com.dvtrung.sound.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public final class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle(getClass().getName());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("res/view/Main.fxml"));
        BorderPane rootPane = loader.load();
        MainController controller = loader.getController();
        controller.rootPane = rootPane;

        final Scene scene = new Scene(rootPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
