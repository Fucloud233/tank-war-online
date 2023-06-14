package com.tankWar.communication.msg;

public class ResetMsg extends InfoMsg{
    int curGameNum = 0;

    public ResetMsg() {
        super(MessageType.Reset);
    }

    public ResetMsg(int mapId, int playerNum,  int curGameNum) {
        super(-1, mapId, playerNum, MessageType.Reset);
        this.curGameNum = curGameNum;
    }

    public int getCurGameNum() { return curGameNum; }

}
