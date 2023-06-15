package com.tankWar.game;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class OverDialog {
    int[] scores;

    Button retRoomBtn = new Button("返回房间");
//    Button retLobbyBtn = new Button("返回大厅");

//    boolean returnRoom;

    Stage primayStage = new Stage();
    HBox mainPane = new HBox();

    OverDialog(int[] scores) {
        Scene scene = new Scene(mainPane);

        primayStage.setTitle("游戏结束!");
        primayStage.setScene(scene);

        this.scores =scores;

        this.initPane();
    }

    void initPane() {
        mainPane.setPrefWidth(300);
        mainPane.setPrefHeight(200);

        // 添加监听
        retRoomBtn.setOnAction(e->{
//            this.returnRoom = true;
            this.primayStage.close();
        });

//        retLobbyBtn.setOnAction(e->{
//            this.isRetRoom = false;
//            this.primayStage.close();
//        });

        // todo 显示结算页面

        // 添加按钮
//        mainPane.getChildren().addAll(retLobbyBtn, retRoomBtn);
        mainPane.getChildren().addAll(retRoomBtn);
    }

    // 返回用户选择结果
    public void display() {
//        System.out.println("show");

        primayStage.showAndWait();
//        return returnRoom;
    }
}
