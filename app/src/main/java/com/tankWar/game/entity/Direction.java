package com.tankWar.game.entity;

public enum Direction {
    INVALID(0),
    CENTER(0x1),
    LEFT(0x10),
    UP(0x100),
    RIGHT(0x1000),
    DOWN(0x10000);

    int value = 0;

    public int getValue() {
        return value;
    }

    Direction(int value) {
        this.value = value;
    }
}
