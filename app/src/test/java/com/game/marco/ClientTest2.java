package com.game.marco;

import com.tankWar.lobby.LoginWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;

public class ClientTest2 extends Application {
    @Test
    public void runClient(){
        Application.launch();
    }

    @Override public void start(Stage primaryStage) throws Exception {
        LoginWindow client = new LoginWindow();
        client.start(primaryStage);
    }
}
