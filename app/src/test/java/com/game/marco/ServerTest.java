package com.game.marco;

import com.tankWar.server.Main;
import org.junit.Test;

import java.net.UnknownHostException;

public class ServerTest {
    @Test public void runServer() throws UnknownHostException {
        Main server=new Main();
        server.start();
    }
}
