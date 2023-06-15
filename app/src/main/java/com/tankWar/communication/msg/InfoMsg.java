package com.tankWar.communication.msg;

public abstract class InfoMsg extends Message{
    int mapId = -1;
    int playerNum = 0;

    public InfoMsg() {
        super(-1, MessageType.Empty);
    }

    public InfoMsg(MessageType type) {
        super(-1, type);
    }

    public InfoMsg(int id, int mapId, int playerNum, MessageType type) {
        super(id, type);
        this.mapId = mapId;
        this.playerNum = playerNum;
    }

    public int getMapId() {
        return mapId;
    }

    public int getPlayerNum() {
        return playerNum;
    }
}
