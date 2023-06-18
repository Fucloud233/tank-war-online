package com.tankWar.server;

public class ServerPrompt {

    public static void infoPlayLeave(String playerName) {
        info("Player " + playerName + " leave!");
    }

    public static void infoAllSendSuccess() {
        info("客户端消息全部发送成功");
    }

    public static void infoBroadcastSuccess() {
        info("广播成功");
    }

    public static void infoGameOver() {
        info("游戏结束");
    }

    public static void info(String text) {
        System.out.println("[info] " + text);
    }
}


