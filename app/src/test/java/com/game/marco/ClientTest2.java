package com.game.marco;

import com.tankWar.lobby.LoginWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;

import java.net.InetAddress;

public class ClientTest2 extends Application {
    @Test
    public void runClient(){
        Application.launch();
    }

    @Override public void start(Stage primaryStage) throws Exception {
        LoginWindow client = new LoginWindow("172.27.62.193");
        client.start(primaryStage);
    }
}
