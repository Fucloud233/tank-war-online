package com.game.fucloud;

import com.tankWar.App;
import com.tankWar.game.GameStatusPane;
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
        GameStatusPane pane = new GameStatusPane();

        pane.setTotalGameNum(5);
        pane.setCurGameNum(3);
        pane.setTotalPlayerNum(2);
        pane.setRestPlayerNum(2);

        Scene scene = new Scene(pane);
        stage.setScene(scene);

        stage.show();

    }
}
