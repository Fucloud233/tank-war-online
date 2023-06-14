package com.tankWar.communication.msg;

public class DeadMsg extends Message{
    public DeadMsg() {
        super(-1, MessageType.Dead);
    }

    public DeadMsg(int id) {
        super(id, MessageType.Dead);
    }
}
