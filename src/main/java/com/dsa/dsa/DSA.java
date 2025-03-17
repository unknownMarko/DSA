package com.dsa.dsa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class DSA extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DSA.class.getResource("dsa_frontend.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 850, 515);
        stage.setTitle("DSA");
        stage.setResizable(false);
        stage.setScene(scene);

        File iconFile = new File("src/main/resources/icon/icon_200.png");
        if (iconFile.exists()) {
            stage.getIcons().add(new Image(iconFile.toURI().toString()));
        } else {
            System.err.println("Could not find icon!");
        }


        Controller controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}