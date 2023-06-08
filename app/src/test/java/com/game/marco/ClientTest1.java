package com.game.marco;

import com.tankWar.game.server.Config;
import com.tankWar.lobby.LoginWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;

import java.net.InetAddress;

public class ClientTest1 extends Application {
    @Test public void runClient(){
        Application.launch();
    }

    @Override public void start(Stage primaryStage) throws Exception {
        LoginWindow client = new LoginWindow(Config.ip);

        client.start(primaryStage);
    }
}