package com.tankWar.game.msg;

// 游戏开始时 Sever用于初始化游戏信息 玩家会获得一个id

public class InitMsg extends InfoMsg{
    int totalGameNum = 0;

    public InitMsg() {
        super(MessageType.Init);
    }

    public InitMsg(int id, int mapId, int playerNum,  int totalGameNum) {
        super(id, mapId, playerNum, MessageType.Init);
        this.totalGameNum = totalGameNum;
    }

    public int getTotalGameNum() { return totalGameNum; }

}
