package com.game.fucloud;

import com.tankWar.game.component.GamePane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

public class GamePane2Test extends Application{
    @Test
    public void runGamePane() {
        System.out.println("hello");
//        Utils.runGameSever(1);

        Application.launch();
    }

    @Override
    public void start(Stage primaryStage) {
        GamePane gamePane = new GamePane();

        Scene scene = new Scene(gamePane);

        primaryStage.setScene(scene);
        primaryStage.setTitle("坦克大战联机版2");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
