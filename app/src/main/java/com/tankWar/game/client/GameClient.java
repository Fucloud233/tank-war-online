package com.tankWar.game.client;

import com.alibaba.fastjson.JSONObject;
import com.tankWar.game.entity.Direction;
import com.tankWar.game.server.Config;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.TimeoutException;

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


