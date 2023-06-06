package com.tankWar.game.msg;

import com.tankWar.game.entity.Direction;

public class MoveMsg extends Message {
    Direction dir;

    public MoveMsg() {
        super(-1, MessageType.Move);
    }

    public MoveMsg(int id, Direction dir) {
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
