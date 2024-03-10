package com.tankWar.client.game;

public class Config {


    // 坦克相关配置
    public final static float TankWidth = 60;
    public final static float TankHeight = 48;
    public final static int TankSpeed = 3;
    public final static int TankMaxBulletNum = 1;

    // 子弹相关配置
    public final static float BulletSize = 16;
    public final static int BulletSpeed = 6;
    public final static double bulletMaxDistance = 180;

    // 地图相关设定
    // 设置地图的最大大小
    public final static int MaxPlayerNumber = 4;

    public final static float MapMaxWidth = 960;
    // 暂时设置为480
    public final static float MapMaxHeight = 480;

    public final static float MapPaddingSize = 10;

    // 刷新率
    public final static long RefreshRate = 1000 / 60;
}
