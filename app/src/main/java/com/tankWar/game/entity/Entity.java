package com.tankWar.game.entity;

public abstract class Entity {
    protected int width, height;
    protected int x, y;
    int speed = 0;
    Direction dir = Direction.CENTER;

    // 构造函数
    Entity(int width, int height) {
        this(width, height, 0, 0);
    }
    Entity(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

    }

    // 用于处理实体之间的碰撞
    boolean isCollidingWith(Entity entity) {
        return false;
    }

    // 移动
    public void move() {

    }

    // 修改方向
    public void setDirection(Direction dir){

    }
}

