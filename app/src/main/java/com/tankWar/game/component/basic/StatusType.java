package com.tankWar.game.component.basic;

// 用于记录状态类型
public enum StatusType {
    StatusTitle("游戏状态"),
    GameNum("局数"),
    PlayerNum("玩家数"),
    LastType("");

    final String text;

    StatusType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}