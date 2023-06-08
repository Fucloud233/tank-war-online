package com.game.marco;

import com.tankWar.game.server.Config;
import com.tankWar.lobby.ChatServer;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTest {
    @Test public void runServer() throws UnknownHostException {
        ChatServer server=new ChatServer(InetAddress.getByName(Config.ip), 8888);
        server.run();
    }
}
