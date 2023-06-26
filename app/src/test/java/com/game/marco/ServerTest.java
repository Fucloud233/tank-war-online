package com.game.marco;

import com.tankWar.server.Main;
import org.junit.Test;

public class ServerTest {
    @Test public void runServer() {
        Main server=new Main();
        server.start();
    }
}
