package com.tankWar.game.server;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.*;

// v1 服务端只提供消息的转发，不负责统一的状态管理
// v2 服务端能够记录状态，并且定时发送状态同步消息

public class GameServer {
    // 记录玩家数量
    int num;

    ServerSocket serverSocket;

    Socket[] sockets;
    DataOutputStream[] out;
    DataInputStream[] in;

    public GameServer(int num) {
        // num为游戏中的玩家数量
        this.num = num;

        // 初始化列表
        sockets = new Socket[num];
        in = new DataInputStream[num];
        out = new DataOutputStream[num];
    }

    // 运行函数
    public void start() throws IOException {
        // 建立TCP连接
        serverSocket = new ServerSocket(Config.port);
        for(int i=0; i<num; i++) {
//            System.out.println("正在等待连接");
            sockets[i] = serverSocket.accept();

            out[i] = new DataOutputStream(sockets[i].getOutputStream());
            in[i] = new DataInputStream(sockets[i].getInputStream());
            System.out.println("服务端已连接" + (i+1));
        }

        // 创建多线程连接业务
        for(int i=0; i<num; i++) {
            ReceiveThread t = new ReceiveThread(i);
            t.start();
        }
    }

    // 广播状态
    void broadcast(int id, String msg) {
        for(int i=0; i<num; i++) {
            if(i == id)
                continue;

            try {
//                System.out.println(msg);
                out[i].writeUTF(msg);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 用来处理线程
    class ReceiveThread extends Thread{
        int id;
        DataInputStream in;

        ReceiveThread(int id) {
            this.id = id;
            try {
                in = new DataInputStream(sockets[id].getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            // 循环接收消息
            while (true)
                handle();
        }

        // 接收消息
        public void handle()  {
            JSONObject jsonMsg;
            // 1. socket接收到JSON消息
            try {
                String msg = in.readUTF();
                System.out.println("来自客户端的消息: " + msg);
                jsonMsg = JSONObject.parseObject(msg);
            }
            catch(IOException e) {
                e.printStackTrace();
                return;
            }

            // 2. 进行验证

            // 3. socket广播消息
            broadcast(id, jsonMsg.toString());
        }

    }
}
