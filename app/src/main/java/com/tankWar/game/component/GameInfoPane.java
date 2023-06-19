package com.tankWar.game.component;

import com.tankWar.game.Config;
import com.tankWar.game.component.basic.ScoreTable;
import com.tankWar.game.component.basic.StatusTable;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

import java.net.URL;

import static com.tankWar.game.component.basic.StatusType.*;

public class GameInfoPane extends VBox {
    // 包含的组件信息
    StatusTable statusTable = new StatusTable("游戏状态");

    ScoreTable scoreTable = new ScoreTable("计分板");

    public GameInfoPane() {
        init();
    }

    public GameInfoPane(String[] playerNames) {
        init();

        // 如果Player不为空
        if (playerNames != null)
            for (String name : playerNames)
                this.scoreTable.addPlayer(name);
    }

    // 初始化上述组件信息
    void init() {
        // Status配置
        statusTable.addMultipleStatus(GameNum, 5);
        statusTable.addMultipleStatus(PlayerNum, 4);

        this.getChildren().addAll(statusTable, scoreTable);

        // 设置样式
        URL styleURL = this.getClass().getResource("/css/label.css");
        if (styleURL != null)
            this.getStylesheets().add(styleURL.toExternalForm());

        // 设置Pane属性
        this.setPrefWidth(150);
        this.setPadding(new Insets(Config.MapPaddingSize));
    }

    /* 封装好的属性设置函数 */
    public void decRestPlayerNum() {
        int num = statusTable.getValue(PlayerNum);
        statusTable.setValue(PlayerNum, num - 1);
    }

    public void incPlayerScore(int winnerID) {
        int num = scoreTable.getValue(winnerID);
        scoreTable.setValue(winnerID, num + 1);
    }

    public void incPlayerScore(String playerName) {
        int num = scoreTable.getValue(playerName);
        scoreTable.setValue(playerName, num + 1);
    }

    /* 底层的属性设置函数 */
    // 用于设置属性
    public void setTotalGameNum(int num) {
        statusTable.setTotalValue(GameNum, num);
    }

    public void setCurGameNum(int num) {
        statusTable.setValue(GameNum, num);
    }

    public void setTotalPlayerNum(int num) {
        statusTable.setTotalValue(PlayerNum, num);
    }

    public void setRestPlayerNum(int num) {
        statusTable.setValue(PlayerNum, num);
    }
}