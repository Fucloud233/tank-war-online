package com.tankWar.game.client.msg;

public class Message {
    int id;
    MessageType type;

    public Message() {
        this.id = -1;
        this.type = MessageType.Empty;
    }

    public Message(int id, MessageType type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
