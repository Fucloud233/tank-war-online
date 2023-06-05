package com.tankWar.game.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.game.msg.InitMsg;
import com.tankWar.game.entity.Tank;
import com.tankWar.game.msg.MessageType;

import java.io.*;
import java.net.*;

// v1 服务端只提供消息的转发，不负责统一的状态管理
// v2 服务端能够记录状态，并且定时发送状态同步消息

public class GameServer {
    // 记录玩家数量
    int num;

    // 剩余玩家数量
    int rest_num;
    // 服务端Socket
    ServerSocket serverSocket;
    // 客户端Socket列表
    Socket[] sockets;
    // 输入输出数据流
    DataOutputStream[] out;
    DataInputStream[] in;

    // 用于处理json绑定
    ObjectMapper mapper = new ObjectMapper();

    // 在游戏房间中构造，包含玩家个数
    public GameServer(int num) {
        // num为游戏中的玩家数量
        this.num = num;

        // 初始化列表
        sockets = new Socket[num];
        in = new DataInputStream[num];
        out = new DataOutputStream[num];
    }

    // 运行函数
    public void run()  {
        // 1.建立TCP连接
        try {
            serverSocket = new ServerSocket(Config.port);

            ServerPrompt.RunSuccess.print();

            for(int i=0; i<num; i++) {
//            System.out.println("正在等待连接");
                sockets[i] = serverSocket.accept();
                // 数据流绑定客户端Socket
                out[i] = new DataOutputStream(sockets[i].getOutputStream());
                in[i] = new DataInputStream(sockets[i].getInputStream());
                System.out.println("服务端已连接" + (i+1));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        // 2.获取并广播初始化信息
        sendInitMsg();

        // 3. 创建多线程连接处理每个客户端连接
        for(int i=0; i<num; i++) {
            ReceiveThread t = new ReceiveThread(i);
            t.start();
        }
    }

    // 发送初始化信息
    void sendInitMsg(){
        // 重置剩余玩家数量
        this.rest_num = num;

        // todo 添加地图信息
//        msg.put("map", )

        // todo 添加坦克信息
        Tank[] tanks = new Tank[2];
        tanks[0] = new Tank(100, 100, 0);
        tanks[1] = new Tank(200, 200, 1);

        // 广播发送所有坦克初始化信息
        try {
            for (int i = 0; i < num; i++) {
                // 配置消息的基本信息
                InitMsg message = new InitMsg(i, tanks);
                // 转换成JSON格式并发送
                String jsonMsg = mapper.writeValueAsString(message);
                out[i].writeUTF(jsonMsg);
            }
            ServerPrompt.AllSend.print();
        } catch(IOException e) {
            ServerPrompt.SendFail.print();
            e.printStackTrace();
        }
    }

    void sendOverMsg() {

    }

    // 用于广播msg状态
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

    // 用来处理接收客户端信息线程
    class ReceiveThread extends Thread{
        int id;
        // 直接使用初始化时创建的输入流即可
//        DataInputStream in;

        ReceiveThread(int id) {
            this.id = id;

//            try {
//                in = new DataInputStream(sockets[id].getInputStream());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }

        void handleDeadMsg() {
            rest_num--;
            // 当只剩下一位玩家则重置游戏
            if(rest_num == 1)
                sendInitMsg();
        }

        @Override
        public void run() {
            // 循环接收消息
            while (true) {
                String msgStr = null;
                // 1. socket接收到JSON消息
                try {
                    msgStr = in[id].readUTF();
                    System.out.println("来自客户端的消息: " + msgStr);
                } catch(SocketException e) {
                    e.printStackTrace();
                    break;
                } catch(IOException e) {
                    continue;
                }

                // 2. 进行验证
                MessageType type = null;
                try {
                    JsonNode json = mapper.readTree(msgStr);
                    type = MessageType.valueOf(json.get("type").asText());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                // 如果是死亡消息 则需要单独处理
                if(type == MessageType.Dead) {
                    this.handleDeadMsg();
                    continue;
                }

                // 3. socket广播消息
                broadcast(id, msgStr);
            }
        }
    }
}
