package com.tankWar.game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{
    public static void main(String[] args) {
        Application.launch(args);
    }
    GamePane gamePane = new GamePane();

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(gamePane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("坦克大战联机版");
        primaryStage.show();
    }
}
