package com.tankWar.game.entity;

/*
    Bullet 子弹类
    由Tank发出，会与建筑方块和其他Tank发生碰撞
 */

public class Bullet extends Entity{
    // 坦克id
    int id;
    int maxDistance;

    Bullet(int width, int height) {
        super(width, height);
    }

    @Override
    public boolean isCollidingWith(Entity entity) {
        return false;
    }
}
