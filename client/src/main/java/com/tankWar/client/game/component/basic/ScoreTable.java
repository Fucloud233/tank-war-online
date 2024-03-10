package com.tankWar.client.game.component.basic;

import com.tankWar.client.game.component.basic.ValueTable;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;

// 显示得分信息的Pane
public class ScoreTable extends ValueTable {
    HashMap<String, Integer> index = new HashMap<>();

    public ScoreTable(String title) {
        super(title);
    }

    public ScoreTable(int playerNum) {
        super(playerNum);
    }

    public ScoreTable(String[] playerNames) {
        super(playerNames == null ? 0 : playerNames.length);

        if (playerNames == null) {
            return;
        }

        for (String name : playerNames) {
            addPlayer(name);
        }
    }

    public ScoreTable(String title, String[] playerNames, int[] scores) {
        super(title, playerNames == null ? 0 : playerNames.length);

        // 1. 名字为空 2. 得分为空 3. 得分不为空 但长度不符合
        if (playerNames == null || scores == null || scores.length != playerNames.length) {
            return;
        }

        for (int i=0; i<playerNames.length; i++)
            addPlayer(playerNames[i], scores[i]);

//        this.setStyle("-fx-background-color: black");
    }

    public ScoreTable(String[] playerNames, int[] scores) {
        this("积分榜", playerNames, scores);
    }

    public void addPlayer(String name) {
        index.put(name, index.size());
        this.addRow(name, new NumberLabel());
    }

    public void addPlayer(String name, int value) {
        index.put(name, index.size());
        this.addRow(name, new NumberLabel(value));
    }

    public void setValue(String name, int value) {
        super.setValue(index.get(name), value);
    }

    public int getValue(String name) {
        return getValue(index.get(name));
    }
}
