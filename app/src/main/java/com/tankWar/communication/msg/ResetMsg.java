package com.tankWar.communication.msg;

// 重置消息中ID 为胜者的ID
public class ResetMsg extends InfoMsg{
    int curGameNum = 0;

    public ResetMsg() {
        super(MessageType.Reset);
    }

    public ResetMsg(int winnerId, int mapId, int playerNum, int curGameNum) {
        super(winnerId, mapId, playerNum, MessageType.Reset);
        this.curGameNum = curGameNum;
    }

    public int getCurGameNum() { return curGameNum; }

}
