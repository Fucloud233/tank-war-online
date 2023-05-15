package com.tankWar.game;

import com.tankWar.game.example.CirclePane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Main extends Application{
    public static void main(String[] args) {
        Application.launch(args);
    }
//    GamePane gamePane = new GamePane();

    @Override
    public void start(Stage primaryStage) {
        CirclePane circlePane = new CirclePane();

        Scene scene = new Scene(circlePane);

        primaryStage.setScene(scene);
        primaryStage.setTitle("坦克大战联机版");
        primaryStage.show();

        circlePane.requestFocus();
    }
}
