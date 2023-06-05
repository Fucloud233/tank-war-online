package com.tankWar.game.entity;

/*
    Building 建筑方块类（对象）
    本游戏的所有建筑物都是正方形，且按照表格排布在地图上

    Block 建筑方块类型
    存储不同类型的方块，记录其相关属性
 */

import com.tankWar.game.Config;
import javafx.scene.image.Image;

public class Building extends Entity{
    Block block;

    // 子弹是否能穿过建筑方块
    public boolean canGoThough(){
        return block.name.equals("empty")||block.name.equals("grass");
    }

    // 子弹是否可击碎
    public boolean isFragile() {
        return block.isFragile;
    }

    public Building(float x, float y, char id) {
        super(Config.BlockSize, Config.BlockSize, x, y);
        // 根据读入文件字符类型设置方块类型
        switch (id) {
            case ' ': block = Block.Empty; break;
            case 'S': block = Block.Stone; break;
            case 'W': block = Block.Wood; break;
            case 'G': block = Block.Grass; break;
            default: {
                System.out.println("ID is illegal!");
                return;
            }
        }
        // 设置贴图
        setImage(block.getImg());
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
    Empty(' ', "empty", false),
    Stone('S', "stone", false),
    Wood('W', "wood", true),
    Grass('G', "grass", false);

    final char id;
    final String name;
    final boolean isFragile;

    Block(char id, String name, boolean isFragile) {
        this.id = id;
        this.name = name;
        this.isFragile = isFragile;
    }

    // todo 获得对应方块的贴图
    public Image getImg() {
        switch(id) {
            case 'S'->{
                return new Image("/image/stone.png");
            }
            case 'W'->{
                return new Image("/image/wood.png");
            }
            case 'G'->{
                return new Image("/image/grass.png");
            }
            default -> {
                return null;
            }
        }
    }
}
