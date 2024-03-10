package com.tankWar.entity;

import javafx.scene.image.Image;

public abstract class Entity {
    protected double width, height;
    protected double x, y;
    protected Image image;
    protected boolean alive = true;
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
    }

    // 用于处理实体之间的碰撞
    public abstract boolean isCollidingWith(Entity entity);

    // 移动
    public abstract  void move();

    // 修改方向
    public void setDirection(Direction dir){
        switch (dir) {
            case LEFT -> setDirection(Direction.LEFT);
            case RIGHT -> setDirection(Direction.RIGHT);
            case UP -> setDirection(Direction.UP);
            case DOWN -> setDirection(Direction.DOWN);
            default -> System.out.println("[error] Direction error");
        }
    }

    public Direction getDir() {
        return dir;
    }

    public abstract Image getImage();

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

    public double getImageX() {
        return x - width/2;
    }

    public double getImageY() {
        return y - height/2;
    }

}