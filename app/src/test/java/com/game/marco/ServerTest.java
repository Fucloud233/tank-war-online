package com.game.marco;

import com.tankWar.server.Main;
import org.junit.Test;

import java.net.UnknownHostException;

public class ServerTest {
    @Test public void runServer()  {
        Main server=new Main(8888);
        server.start();
    }
}
