package com.tankWar.game.msg;

import com.tankWar.game.entity.Direction;

public class ShootMsg extends Message {
    Direction dir;
    int x, y;

    public ShootMsg(int id, Direction dir, int x, int y) {
        super(id, MessageType.Shoot);
        this.dir = dir;
        this.x = x;
        this.y = y;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}