package com.tankWar.game.msg;

// 与Tank解耦合
public class TankInfo {
    int id;
    double x, y;

    TankInfo() {}

    public TankInfo(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
