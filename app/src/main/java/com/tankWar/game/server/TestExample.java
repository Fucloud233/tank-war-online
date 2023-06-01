package com.tankWar.game.server;

import com.tankWar.game.entity.Tank;

public class TestExample{
    public static Tank[] getTestTankInfos() {

        Tank[] tanks = new Tank[2];
        tanks[0] = new Tank(50, 50, 0);
        tanks[1] = new Tank(200, 200, 1);

        return tanks;
    }
}
