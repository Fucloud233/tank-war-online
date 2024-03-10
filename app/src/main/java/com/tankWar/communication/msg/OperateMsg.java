package com.tankWar.communication.msg;

import com.tankWar.entity.Direction;

public abstract class OperateMsg extends Message{
    protected Direction dir = Direction.INVALID;
    protected double x = 0, y = 0;

    public OperateMsg(MessageType type) {
        super(-1, type);
    }

    public OperateMsg(int id, Direction dir, double x, double y, MessageType type) {
        super(id, type);
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
