package com.game.fucloud.component;

import com.tankWar.game.component.GameInfoPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

public class GameStatusPaneTest extends Application {
    @Test
    public void testRun() {
        Application.launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // 用户名称
        String[] names = new String[]{
                "Tom", "Jack", "Alex"
        };

        GameInfoPane pane = new GameInfoPane(names);

        // 设置状态栏的数值
        pane.setTotalGameNum(5);
        pane.setCurGameNum(3);
        pane.setTotalPlayerNum(2);
        pane.setRestPlayerNum(2);

        // 设置积分榜的数值
        pane.incPlayerScore(names[0]);
        pane.incPlayerScore(names[2]);

        Scene scene = new Scene(pane);
        stage.setScene(scene);

        stage.show();
    }
}
