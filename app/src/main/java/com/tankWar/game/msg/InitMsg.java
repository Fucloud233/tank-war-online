package com.tankWar.game.msg;

// 游戏开始时 Sever用于初始化游戏信息 玩家会获得一个id

public class InitMsg extends Message{
    int mapId = -1;
    int totalGameNum = 0;


    public InitMsg() {
        super(-1, MessageType.Init);
    }

    public InitMsg(int id, int mapId) {
        super(id, MessageType.Init);
        this.mapId = mapId;
    }

    public InitMsg(int id, int mapId, int totalGameNum) {
        super(id, MessageType.Init);
        this.totalGameNum = totalGameNum;
        this.mapId = mapId;
    }

    public int getMapId() {
        return mapId;
    }

    public int getTotalGameNum() { return totalGameNum; }

}
