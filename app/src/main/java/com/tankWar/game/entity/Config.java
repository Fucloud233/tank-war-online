package com.tankWar.game.entity;

public class Config {
    // 建筑方块相关配置
    public final static int BlockSize = 32;


    // 坦克相关配置
    public final static int TankWidth = 60;
    public final static int TankHeight = 48;
    public final static int TankSpeed = 5;


    // 子弹相关配置
    public final static int BulletSize = 16;
    public final static int BulletSpeed = 50;

    // 地图相关设定
    public final static int BlockXNumber = 30;
    public final static int BlockYNumber = 15;
    public final static int MapPaddingSize = 10;
    public final static int MapWidth = BlockXNumber * BlockSize;
    public final static int MapHeight = BlockYNumber * BlockSize;


}
