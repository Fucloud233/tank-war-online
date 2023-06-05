package com.tankWar.game.msg;

import com.tankWar.game.entity.Direction;

public class ShootMsg extends Message {
    Direction dir = Direction.INVALID;
    double x = 0, y = 0;

    public ShootMsg() {
        super(-1, MessageType.Shoot);
    }

    public ShootMsg(int id, Direction dir, double x, double y) {
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

    public double getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
