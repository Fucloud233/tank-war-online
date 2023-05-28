package com.tankWar.game.entity;

public class Config {
    // 建筑方块相关配置
    public final static float BlockSize = 32;

    // 坦克相关配置
    public final static float TankWidth = 60;
    public final static float TankHeight = 48;
    public final static int TankSpeed = 3;

    // 子弹相关配置
    public final static float BulletSize = 16;
    public final static int BulletSpeed = 6;
    public final static double bulletMaxDistance = 180;

    // 地图相关设定
    public static int BlockXNumber;
    public static int BlockYNumber;
    public final static float MapPaddingSize = 10;
    public static float MapWidth = BlockXNumber * BlockSize;
    public static float MapHeight = BlockYNumber * BlockSize;
}
