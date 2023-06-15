package com.tankWar.communication.msg;

import com.tankWar.game.entity.Direction;

public class MoveMsg extends OperateMsg {
    public MoveMsg() {
        super(MessageType.Move);
    }

    public MoveMsg(int id, Direction dir, double x, double y) {
        super(id, dir, x, y, MessageType.Move);
    }
}
