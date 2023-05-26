package com.tankWar.game.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class Entity {
    protected double width, height;
    protected double x, y;
    protected Image image;
    protected int speed = 0;
    protected boolean alive;
    protected Direction dir = Direction.CENTER;

    // 构造函数
    Entity(double width, double height) {
        this(width, height, 0, 0);
    }
    Entity(double width, double height, double x, double y) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.alive=true;
    }

    // 用于处理实体之间的碰撞
    public boolean isCollidingWith(Entity entity) {
        return false;
    }

    // 移动
    public void move() {

    }

    // 修改方向
    public void setDirection(Direction dir){

    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void draw(GraphicsContext graphic){
        if(alive){
            graphic.drawImage(image, x-this.width/2, y-this.height/2);
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

}

