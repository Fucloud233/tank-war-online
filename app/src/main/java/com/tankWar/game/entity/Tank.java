package com.tankWar.game.entity;

/*
    Tank 坦克类
 */

import javafx.scene.image.Image;

public class Tank extends Entity{
    // id 玩家编号
    private int id;
    // 坦克不同方向照片
    private Image imageUp = new Image("/image/tankUp.png");
    private Image imageDown = new Image("/image/tankDown.png");
    private Image imageLeft = new Image("/image/tankLeft.png");
    private Image imageRight = new Image("/image/tankRight.png");

    // tank构造函数(随机方向)
    public Tank(double x, double y) {
        super(Config.TankWidth, Config.TankHeight, x, y);
        // 随机坦克方向
        double random = Math.random();
        if(random<=0.25){
            setImage(imageLeft);
            dir = Direction.LEFT;
        } else if (random <= 0.5) {
            setImage(imageRight);
            dir = Direction.RIGHT;
        } else if (random <= 0.75) {
            setImage(imageUp);
            dir = Direction.UP;
        } else {
            setImage(imageDown);
            dir = Direction.DOWN;
        }
    }

    // tank构造函数(带方向)
    public Tank(double x, double y, Direction dir) {
        super(Config.TankWidth, Config.TankHeight, x, y);
        switch (dir){
            case UP: setImage(imageUp); dir = Direction.UP; break;
            case DOWN: setImage(imageDown); dir = Direction.DOWN; break;
            case LEFT: setImage(imageLeft); dir = Direction.LEFT; break;
            case RIGHT: setImage(imageRight); dir = Direction.RIGHT; break;
            default:
                System.out.println("Direction error");
        }
    }

    // 坦克移动函数
    public void move(Direction dir, int speed) {
        if(dir == Direction.LEFT){
            this.dir = Direction.LEFT;
            setImage(imageLeft);
            x = x - speed;
        } else if(dir == Direction.RIGHT){
            this.dir = Direction.RIGHT;
            setImage(imageRight);
            x = x + speed;
        } else if(dir == Direction.UP){
            this.dir = Direction.UP;
            setImage(imageUp);
            y = y - speed;
        } else if(dir == Direction.DOWN){
            this.dir = Direction.DOWN;
            setImage(imageDown);
            y = y + speed;
        }
    }
}
