package com.tankWar.game;

import java.util.HashMap;

import org.javatuples.Pair;

public class Config {
    // 建筑方块相关配置
    public final static float BlockSize = 32;

    // 坦克相关配置
    public final static float TankWidth = 60;
    public final static float TankHeight = 48;
    public final static int TankSpeed = 3;
    public final static int TankMaxBulletNum = 1;

//    public final static HashMap<Integer, String> TankColorMap = new HashMap<Integer, String>() {{
//        put(0, "green");
//        put(1, "red");
//        put(2, "white");
//        put(3, "blue");
//    }};

    public final static HashMap<Integer, Pair<String, String>> TankColorMap = new HashMap<Integer, Pair<String, String>>() {{
        put(0, Pair.with("green", "00FF00"));
        put(1, Pair.with("red", "FF0000"));
        put(2, Pair.with("white", "FFFFFF"));
        put(3, Pair.with("blue", "0000FF"));
    }};

    // 子弹相关配置
    public final static float BulletSize = 16;
    public final static int BulletSpeed = 6;
    public final static double bulletMaxDistance = 180;

    // 地图相关设定
    // 设置地图的最大大小
    public final static float MapMaxWidth = 960;
    // 暂时设置为480
    public final static float MapMaxHeight = 480;

    public final static float MapPaddingSize = 10;

    // 刷新率
    public final static long RefreshRate = 1000 / 60;
}
