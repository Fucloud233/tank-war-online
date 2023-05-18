package com.tankWar.game.entity;

/*
    Building 建筑方块类（对象）
    本游戏的所有建筑物都是正方形，且按照表格排布在地图上

    Block 建筑方块类型
    存储不同类型的方块，记录其相关属性
 */

import javafx.scene.paint.Color;

public class Building extends Entity{
    Block block;

    Building(int x, int y) {
        super(Config.BlockSize, Config.BlockSize, x, y);
    }
}

// 使用枚举类型来记录不同类型的建筑方块
enum Block {
    Null(0, "空", false, Color.WHITE),
    Stone(1, "石头", false, Color.GRAY),
    Wood(2, "木头", true, Color.BROWN);

    final int id;
    final String name;
    final boolean isFragile;
    final Color color;

    Block(int id, String name, boolean isFragile, Color color) {
        this.id = id;
        this.name = name;
        this.isFragile = isFragile;
        this.color = color;
    }

    public boolean isFragile() {
        return isFragile;
    }

    // todo 获得对应方块的贴图
    public void getImg() {

    }

    // 由于暂时没有考虑相关贴图 因此这里用不同颜色代替
    public Color getColor() {
        return this.color;
    }
}
