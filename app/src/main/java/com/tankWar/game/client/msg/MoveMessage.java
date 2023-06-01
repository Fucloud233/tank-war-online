package com.tankWar.game.client.msg;

import com.tankWar.game.entity.Direction;

public class MoveMessage extends Message {
    Direction dir;

    public MoveMessage() {
        super(-1, MessageType.Move);
    }

    public MoveMessage(int id, Direction dir) {
        super(id, MessageType.Move);
        this.dir = dir;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }
}
