package com.tankWar.server;

import java.nio.channels.SocketChannel;
import java.util.Vector;

public class Game {
    // 所有玩家
    Vector<SocketChannel> sockets;
    Vector<Integer> restPlayer;

    // 记录局数信息
    int totalGameNum = 2, curGameNum = 1;

    // 记录分数
    int[] scores;
    // 地图id
    int mapId = 0;

    public int getMapId() {
        return mapId;
    }

    public Game(Vector<SocketChannel> sockets) {
        int size = sockets.size();

        // 初始化数组
        this.sockets = sockets;
        restPlayer = new Vector<>(size);
        scores = new int[size];

        for(int i=0; i<size; i++)
            restPlayer.addElement(i++);
    }

    void resetRestPlayer() {
        restPlayer.clear();

        for(int i=0; i<sockets.size(); i++) {
            restPlayer.addElement(i);
        }
    }

    // 记录死亡 并当只剩下一个人的时候返回true
    public DeadStatus dead(int id) {
        // 1. 记录死亡消息
        restPlayer.removeElement(id);

        if(restPlayer.size() != 1)
            return DeadStatus.Null;

        // 2. 当剩下一位玩家则重置游戏
        int winnerId = restPlayer.get(0);

        // 重置死亡信息
        resetRestPlayer();

        // 3. 加分
        scores[winnerId]++ ;

        // 4. 判断是否是最后场游戏
        if(curGameNum < totalGameNum) {
            curGameNum++;
            return DeadStatus.GameOver;
        }

        return DeadStatus.MatchOver;
    }

    public int getTotalPlayerNum() {
        return sockets.size();
    }

    public int getRestPlayerNum() {
        return restPlayer.size();
    }

    // 得到总局数
    public int getTotalGameNum() {
        return totalGameNum;
    }

    // 得到总局数
    public int getCurGameNum() {
        return curGameNum;
    }

    // 得到分数
    public int[] getScores() {
        return scores;
    }

    // 得到所有玩家
    public Vector<SocketChannel> getAllSockets() {
        return sockets;
    }
}

enum DeadStatus{
    // 无事发生
    Null,
    // 一场游戏结束
    GameOver,
    // 所有常数结束
    MatchOver;

    public boolean shouldReset() {
        return this == GameOver;
    }

    public boolean shouldOver() {
        return this == MatchOver;
    }
}