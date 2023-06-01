package com.tankWar.game.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.game.client.msg.InitMessage;
import com.tankWar.game.entity.Tank;

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

    // 用于处理json绑定
    ObjectMapper mapper = new ObjectMapper();
    
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
        // 1.建立TCP连接
        serverSocket = new ServerSocket(Config.port);
        for(int i=0; i<num; i++) {
//            System.out.println("正在等待连接");
            sockets[i] = serverSocket.accept();

            out[i] = new DataOutputStream(sockets[i].getOutputStream());
            in[i] = new DataInputStream(sockets[i].getInputStream());
            System.out.println("服务端已连接" + (i+1));
        }

        // 2.获取并广播初始化信息
        initInfo();

        // 3. 创建多线程连接业务
        for(int i=0; i<num; i++) {
            ReceiveThread t = new ReceiveThread(i);
            t.start();
        }
    }

    // 发送初始化信息
    void initInfo(){

        // todo 添加地图信息
//        msg.put("map", )

        // todo 添加坦克信息
        Tank[] tanks = TestExample.getTestTankInfos();

        // 广播发送所有坦克信息
        try {
            for (int i = 0; i < num; i++) {
                // 配置消息的基本信息
                InitMessage message = new InitMessage(1, tanks);
                // 转换成JSON格式并发送
                String jsonMsg = mapper.writeValueAsString(message);
                out[i].writeUTF(jsonMsg);
            }
            ServerPrompt.AllSent.print();
        } catch(IOException e) {
            ServerPrompt.SendFail.print();
            e.printStackTrace();
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
            String msg = null;
            // 1. socket接收到JSON消息
            try {
                msg = in.readUTF();
                System.out.println("来自客户端的消息: " + msg);
            }
            catch(IOException e) {
                e.printStackTrace();
                return;
            }

            // 2. 进行验证

            // 3. socket广播消息
            broadcast(id, msg);
        }
    }
}
