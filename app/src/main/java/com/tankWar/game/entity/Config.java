package com.tankWar.game.entity;

public class Config {
    // 建筑方块相关配置
    public final static int BlockSize = 32;


    // 坦克相关配置
    public final static int TankWidth = 60;
    public final static int TankHeight = 48;
    public final static int TankSpeed = 3;


    // 子弹相关配置
    public final static int BulletSize = 16;
    public final static int BulletSpeed = 6;

    // 地图相关设定
    public static int BlockXNumber;
    public static int BlockYNumber;
    public final static int MapPaddingSize = 10;
    public static int MapWidth = BlockXNumber * BlockSize;
    public static int MapHeight = BlockYNumber * BlockSize;


}
