package com.tankWar.game.msg;

import com.tankWar.game.entity.Direction;

public class ShootMsg extends OperateMsg {
    public ShootMsg() {
        super(MessageType.Shoot);
    }

    public ShootMsg(int id, Direction dir, double x, double y) {
        super(id, dir, x, y, MessageType.Shoot);
    }
}
