package com.tankWar.game.client;

import com.tankWar.game.server.Config;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

// 创建两个线程: 发送和接收
// 当有键盘事件的时候 就要进行发送
// 随时准备接收 这个是异步的

// 客户端创建两个线程（使用NIO）
// 发送：提供接口传输类型
// 接收：直接修改状态，数据怎么回调

// 服务端n个线程 有多少个玩家
// 接收后 立即广播

public class GameClient {
    Socket clientSocket;

    public GameClient()  {
    }

    // 初始化连接 连接正常就不会抛出异常
    public void connect() throws IOException, TimeoutException {
        clientSocket = new Socket(Config.ip, Config.port);
        clientSocket.setSoTimeout(1000);
    }

    // 发送消息
    public void send(Message msg) throws IOException {
        OutputStream outToServer = clientSocket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        out.writeUTF("ID: " + msg.id);
    }

    // 接收状态(需要额外开辟一个线程)
    public Vector<Message> receive() {

        return new Vector<Message>();
    }
}


