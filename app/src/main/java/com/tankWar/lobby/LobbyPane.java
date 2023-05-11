package com.tankWar.lobby;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class LobbyPane extends Pane{
    LobbyPane() {
        this.setMinWidth(400);
        this.setMinHeight(300);

            //

        // 测试代码 (可以删除)
        Label label = new Label("hello world!");
        this.getChildren().add(label);
    }
}
