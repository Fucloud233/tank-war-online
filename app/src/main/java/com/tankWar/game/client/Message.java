package com.tankWar.game.client;

public class Message {
    int id;
    Command cmd;

    public Message(int id, Command cmd) {
        this.id = id;
        this.cmd = cmd;
    }

    public Command getCMD() {
        return cmd;
    }

    public int getID() {
        return id;
    }
}
