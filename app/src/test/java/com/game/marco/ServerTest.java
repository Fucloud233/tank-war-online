package com.game.marco;

import com.tankWar.server.Main;
import org.junit.Test;

public class ServerTest {
    @Test public void runServer() {
        // 添加了适当延时
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ignored){};

        Main server=new Main();
        server.start();
    }
}
