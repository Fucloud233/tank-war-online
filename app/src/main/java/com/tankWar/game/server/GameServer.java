package com.tankWar.game.server;

import com.tankWar.game.server.Config;

import java.io.*;
import java.net.*;

// v1 服务端只提供消息的转发，不负责统一的状态管理
// v2 服务端能够记录状态，并且定时发送状态同步消息



public class GameServer {
    // 记录玩家数量
    int num;

    ServerSocket serverSocket;
    Socket[] sockets;

    public GameServer(int num) {
        this.num = num;
        sockets = new Socket[num];
    }

    // 运行函数
    public void start() throws IOException {
        serverSocket = new ServerSocket(Config.port);

        for(int i=0; i<num; i++) {
            System.out.println("正在等待连接");
            sockets[i] = serverSocket.accept();
            System.out.println("已连接" + (i+1));
        }

        // 创建多线程连接业务
        for(int i=0; i<num; i++) {
            HandlerThread t = new HandlerThread(i);
            t.start();
        }
    }

    // 用来处理线程
    class HandlerThread extends Thread{
        int id;
        DataInputStream in;
        DataOutputStream out;

        HandlerThread(int id) {
            this.id = id;
            try {
                in = new DataInputStream(sockets[id].getInputStream());
                out = new DataOutputStream(sockets[id].getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            // 循环接收消息
            while (true)
                handle(id);
        }

        // 接收消息
        public void handle(int id) {
            // socket 接收到消息
            try {
                System.out.println("来自客户端的消息: " + in.readUTF());
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            // socket 广播消息
        }
    }
}
