package com.tankWar.lobby;

import com.tankWar.App;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    // rootPane 之后需要使用该Pane进行多Pane切换
//    BorderPane rootPane = new BorderPane();

    // 大厅窗格 处理大厅业务的主要逻辑
    LobbyPane lobbyPane = new LobbyPane();

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(lobbyPane);

//        rootPane.setCenter(lobbyPane);

        primaryStage.setTitle("游戏大厅");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
