package com.tankWar.game.component;

import com.tankWar.game.component.basic.ScoreTable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Vector;

public class OverDialog {
    double Width = 200, Height = 100;

    // 积分榜
    Stage primayStage = new Stage();
    VBox mainPane = new VBox();

    ScoreTable scoreTable;
    Label winnerLabel;
    Button retRoomBtn = new Button("返回房间");

    // 数值
    String[] playerNames;
    int[] scores;

    public OverDialog(String[] playerNames, int[] scores) {
        this.playerNames = playerNames;
        this.scores = scores;
        // 实现窗口的动态变化
        this.Height += (playerNames.length-1) * 25;

        this.init();

        // 设置窗口
        Scene scene = new Scene(mainPane);

        primayStage.setTitle(null);
        primayStage.setScene(scene);
        primayStage.setResizable(false);
    }

    void init() {
        URL styleURL = this.getClass().getResource("/css/label.css");
        if (styleURL != null)
            mainPane.getStylesheets().add(styleURL.toExternalForm());

        // 设置WinnerLabel
        String[] winnerNames = findWinnerNames();
        StringBuilder winnerText = new StringBuilder();
        if(winnerNames!=null) {
            winnerText.append("胜者是：");
            for(String winnerName: winnerNames)
                winnerText.append(winnerName).append(", ");
        } else {
            winnerText.append("游戏平局！");
        }

        this.winnerLabel = new Label(winnerText.toString());

        // 设置ScoreTable
        // [important] 要在label后面设置
        this.scoreTable = new ScoreTable("积分榜", playerNames, scores);

        // 设置Button
        retRoomBtn.setOnAction(e->{
            this.primayStage.close();
        });

        // 设置mainPane
        mainPane.setPrefWidth(Width);
        mainPane.setPrefHeight(Height);
        mainPane.getChildren().addAll(scoreTable, winnerLabel, retRoomBtn);
        mainPane.setAlignment(Pos.TOP_LEFT);
        mainPane.setSpacing(20);
        mainPane.setPadding(new Insets(20));
    }

    // 根据scores对playerNames排序
    void sortPlayer() {
        int len = playerNames.length;
        for(int i=0; i<len; i++) {
            for(int j=0; j<len-i-1; j++) {
                if(scores[j]<scores[j+1]) {
                    String temp_name = playerNames[j];
                    playerNames[j] = playerNames[j+1];
                    playerNames[j+1] = temp_name;

                    int temp_score = scores[j];
                    scores[j] = scores[j+1];
                    scores[j+1] = temp_score;
                }
            }
        }
    }

    String[] findWinnerNames() {
        this.sortPlayer();

        int len = playerNames.length;

        // 记录赢家 (即得分最高且相同的人)
        Vector<Integer> winners = new Vector<>();
        winners.add(0);
        for(int i=1; i<len; i++) {
            if(scores[i]!=scores[i-1])
                break;
            winners.add(i);
        }

        // 如果所有人都是赢家 在说明平局
        // 则返回空
        if(winners.size()==len) {
            return null;
        }

        // 返回赢家的名称
        String[] winnerNames = new String[winners.size()];
        for(int i=0; i<winnerNames.length; i++)
            winnerNames[i] = playerNames[winners.get(i)];

        return winnerNames;
    }


    // 返回用户选择结果
    public void display() {
//        System.out.println("show");

        primayStage.showAndWait();
//        return returnRoom;
    }
}
