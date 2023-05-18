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

    @Override
    public void start(Stage primaryStage) {
//        CirclePane circlePane = new CirclePane();
        GamePane gamePane = new GamePane();

        Scene scene = new Scene(gamePane);

        primaryStage.setScene(scene);
        primaryStage.setTitle("坦克大战联机版");
        primaryStage.show();

//        circlePane.requestFocus();
    }
}
