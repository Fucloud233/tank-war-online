package com.game.fucloud.other;

import org.ini4j.Ini;
import org.ini4j.Profile;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class INITest {
    @Test
    public void readTest() throws IOException {
        String path = "config.ini";
        File file = new File(path);
        Ini ini = new Ini(file);

        Profile.Section serverSection = ini.get("server");
        if(serverSection == null) {
            System.out.println("[error] Error配置不存在");
            return;
        }

        // 不存在时 则会返回空
        String ip = serverSection.get("ip");
        int port = Integer.parseInt(serverSection.get("port"));

        System.out.printf("ip: %s, port: %d\n", ip, port);
    }
}
