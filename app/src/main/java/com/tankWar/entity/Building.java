package com.tankWar.entity;

/*
    Building 建筑方块类（对象）
    本游戏的所有建筑物都是正方形，且按照表格排布在地图上

    Block 建筑方块类型
    存储不同类型的方块，记录其相关属性
 */

import com.tankWar.utils.GameConfig;
import javafx.scene.image.Image;

public class Building extends Entity{
    Block block;

    // 子弹是否能穿过建筑方块
    public boolean canGoThough(){
        return block.getCanThrough();
    }

    // 子弹是否可击碎
    public boolean isFragile() {
        return block.isFragile;
    }

    public Building(float x, float y, char id) {
        super(GameConfig.BlockSize, GameConfig.BlockSize, x, y);
        // 根据读入文件字符类型设置方块类型
        switch (id) {
            case 'S' -> block = Block.Stone;
            case 'W' -> block = Block.Wood;
            case 'G' -> block = Block.Grass;
            default -> {
                System.out.println("[error] ID is illegal!");
            }
        }
    }

    public Building(Building building) {
        super(GameConfig.BlockSize, GameConfig.BlockSize, building.x, building.y);
        this.block = building.block;
    }

    @Override
    public Image getImage() {
        return this.block.getImg();
    }

    @Override
    public boolean isCollidingWith(Entity entity) {
        return false;
    }

    @Override
    public void move() {
    }
}

// 使用枚举类型来记录不同类型的建筑方块
enum Block {
    // 不需要 Empty
    Stone('S', false),
    Wood('W', true),
    Grass('G', false, true);

    final char id;
    final boolean isFragile;
    final boolean canThrough;
    final Image image;

    Block(char id, boolean isFragile, boolean canThrough) {
        this.id = id;
        this.canThrough = canThrough;
        this.isFragile = isFragile;

        String path = "/image/block/" + this + ".png";
        System.out.println("[test] Path: " + path);

        this.image = new Image(path);

    }

    Block(char id, boolean isFragile) {
        this(id, isFragile, false);
    }

    public Image getImg() {
        return image;
    }

    public boolean getCanThrough() {
        return canThrough;
    }
}
