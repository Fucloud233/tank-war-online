package com.tankWar.game.msg;

public class InitMsg extends Message{
    TankInfo[] tanks = null;
    int mapId = -1;

    public InitMsg() {
        super();
    }

    public InitMsg(int id, int mapId, TankInfo[] tanks) {
        super(id, MessageType.Init);
        this.tanks = tanks;
        this.mapId = mapId;
    }

    public TankInfo[] getTanks() {
        return tanks;
    }

    public int getMapId() {
        return mapId;
    }
}
