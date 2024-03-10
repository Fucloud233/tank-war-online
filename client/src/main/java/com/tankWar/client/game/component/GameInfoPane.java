package com.tankWar.client.game.component;

import com.tankWar.client.game.Config;
import com.tankWar.client.game.component.basic.ScoreTable;
import com.tankWar.client.game.component.basic.StatusTable;
import com.tankWar.client.game.component.basic.TitleLabel;
import com.tankWar.entity.Direction;
import com.tankWar.entity.Tank;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;

import static com.tankWar.client.game.component.basic.StatusType.GameNum;
import static com.tankWar.client.game.component.basic.StatusType.*;

public class GameInfoPane extends VBox {
    // 信息
    int id = -1;
    String[] playerNames = null;

    // 包含的组件信息
    ImageView tankView;
    StatusTable statusTable = new StatusTable("游戏状态");
    ScoreTable scoreTable = new ScoreTable("计分板");

    public GameInfoPane() {
        init();
    }

    public GameInfoPane(int id, String[] playerNames) {
        this.id = id;
        this.playerNames =playerNames;

        init();
    }

    public GameInfoPane(String[] playerNames) {
        this.playerNames =playerNames;

        init();
    }

    // 初始化上述组件信息
    void init() {
        // TankView配置
        if(id != -1) {
            TitleLabel playerNameLabel = new TitleLabel("你的坦克");
            this.tankView = new ImageView(Tank.getImage(this.id, Direction.RIGHT));
            this.getChildren().addAll(playerNameLabel, tankView);
        }

        // Status配置
        statusTable.addMultipleStatus(GameNum, 5);
        statusTable.addMultipleStatus(PlayerNum, 4);

        this.getChildren().addAll(statusTable, scoreTable);

        // 如果Player不为空
        if (playerNames != null)
            for (String name : playerNames)
                this.scoreTable.addPlayer(name);

        // 设置样式
        URL styleURL = this.getClass().getResource("/css/label.css");
        if (styleURL != null)
            this.getStylesheets().add(styleURL.toExternalForm());

        // 设置Pane属性
        this.setPrefWidth(150);
        this.setSpacing(10);
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