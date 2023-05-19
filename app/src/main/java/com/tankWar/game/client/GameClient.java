package com.tankWar.game.client;

import com.alibaba.fastjson.JSONObject;
import com.tankWar.game.entity.Direction;
import com.tankWar.game.server.Config;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.TimeoutException;


/*
    客户端存在两个个线程: 发送线程和接收线程
        发送部分可以写在主线程内, 即移动或摧毁时可以主动发送"消息" (NIO?)
        接收部分则需要额外开辟线程, 负责异步接收"状态变化" (考虑如何回调)
 */

/*
    消息发送者: id
    消息类型:
    1. 指令（移动,发送子弹）
        移动：方向
        发送子弹：发送位置，发送方向
    2. 消息（摧毁坦克, 摧毁方块, 终止游戏）
        摧毁坦克ID
        摧毁方块位置
    消息内容: ...
 */

// https://www.runoob.com/w3cnote/java-json-instro.html
// http://c.biancheng.net/view/6114.html

public class GameClient {
    Socket clientSocket;

    public GameClient()  {
    }

    // 初始化连接 连接正常就不会抛出异常
    public void connect() throws IOException, TimeoutException {
        clientSocket = new Socket(Config.ip, Config.port);
        clientSocket.setSoTimeout(1000);
    }

    // 发送移动消息
    public void sendMove(Direction dir, int id) {
    }

    // 发送发射消息
    public void sendShoot(Direction dir, int id, int x, int y) {

    }

    // 发送坦克被摧毁
    public void sendTankDestroyed(int id) {

    }

    // 发送建筑物被摧毁了
    public void sendBuildingDestroyed(int x, int y) {

    }

    // 发送消息
    void send(JSONObject obj) throws IOException {
        OutputStream outToServer = clientSocket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
//        out.writeUTF("ID: " + msg.id);
    }

    // 接收状态(需要额外开辟一个线程)
    JSONObject receive() {

        return new JSONObject();
    }
}


