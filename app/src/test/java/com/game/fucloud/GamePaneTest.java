package com.game.fucloud;

import com.tankWar.game.GamePane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testng.annotations.BeforeTest;

public class GamePaneTest extends Application {
    @Test
    public void runGamePane() {
        System.out.println("hello");
        Utils.runGameSever(1);
        Application.launch();
    }

    @Override
    public void start(Stage primaryStage) {
        GamePane gamePane = new GamePane();

        Scene scene = new Scene(gamePane);

        primaryStage.setScene(scene);
        primaryStage.setTitle("坦克大战单用户测试");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
