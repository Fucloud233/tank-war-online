package com.tankWar.game.msg;

public class DeadMsg extends Message{
    DeadMsg() {
        super(-1, MessageType.Dead);
    }
}
