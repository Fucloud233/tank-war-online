package com.tankWar.game.entity;

/*
    Tank 坦克类
 */

public class Tank extends Entity{
    // id 玩家编号
    int id;

    Tank(int x, int y) {
        super(Config.TankWidth, Config.TankHeight, x, y);
    }
}
