package com.game.fucloud.component;

import com.tankWar.lobby.SettingStage;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;

public class SettingStageTest extends  Application {
    @Test
    public void testRun() {
        Application.launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        SettingStage s = new SettingStage("123", 123);
        s.showAndWait();
//        stage.show();
    }
}
