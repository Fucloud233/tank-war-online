package com.tankWar.game.entity;

/*
    Building 建筑方块类（对象）
    本游戏的所有建筑物都是正方形，且按照表格排布在地图上

    Block 建筑方块类型
    存储不同类型的方块，记录其相关属性
 */

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Building extends Entity{
    Block block;

    public boolean canGoThough(){
        if(block.name=="empty"||block.name=="grass") return true;
        return false;
    }

    public Building(int x, int y, char id) {
        super(Config.BlockSize, Config.BlockSize, x, y);
        switch (id) {
            case ' ': block = Block.Empty; break;
            case 'S': block = Block.Stone; break;
            case 'W': block = Block.Wood; break;
            case 'G': block = Block.Grass; break;
            default:
                System.out.println("ID is illegal!");
        }
        setImage(block.getImg());
    }

    @Override
    public void draw(GraphicsContext graphic) {
        if(block.name=="empty") return;
        super.draw(graphic);
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
//        this.color = color;
    }

    public boolean isFragile() {
        return isFragile;
    }

    // todo 获得对应方块的贴图
    public Image getImg() {
        if(name=="stone"){
            return new Image("/image/stone.png");
        } else if (name == "wood") {
            return new Image("/image/wood.png");
        } else if (name == "grass") {
            return new Image("/image/grass.png");
        }
        return null;
    }

}
