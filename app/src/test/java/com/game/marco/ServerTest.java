package com.game.marco;

import com.tankWar.server.LobbyServer;
import org.junit.Test;

import java.net.UnknownHostException;

public class ServerTest {
    @Test public void runServer() throws UnknownHostException {
        LobbyServer server=new LobbyServer();
        server.run();
    }
}
