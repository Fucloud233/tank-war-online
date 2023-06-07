package com.game.marco;

import com.tankWar.lobby.ChatServer;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTest {
    @Test public void runServer() throws UnknownHostException {
        ChatServer server=new ChatServer(InetAddress.getLocalHost(), 8888);
        server.run();
    }
}
