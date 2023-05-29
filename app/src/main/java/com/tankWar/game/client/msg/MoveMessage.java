package com.tankWar.game.client.msg;

import com.tankWar.game.entity.Direction;

public class MoveMessage extends Message {
    Direction dir;

    public MoveMessage(int id, MessageType type, Direction dir) {
        super(id, type);
        this.dir = dir;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }
}
