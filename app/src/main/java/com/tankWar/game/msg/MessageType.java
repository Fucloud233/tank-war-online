package com.tankWar.game.msg;


public enum MessageType {
    Empty,

    // 游戏开始时，用来初始化游戏信息
    Init,
    Move,
    Shoot,
    // 玩家的死亡消息
    Dead,
    // 游戏结束消息
    Over,
}
