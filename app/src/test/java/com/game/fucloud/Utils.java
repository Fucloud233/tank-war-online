package com.game.fucloud;

import com.tankWar.game.server.GameServer;

import java.io.IOException;

public class Utils {
    public static void runGameSever(int size) {
        Thread t = new Thread(()->{
            GameServer server = new GameServer(size);
            server.run();
        });

        t.start();
    }
}
