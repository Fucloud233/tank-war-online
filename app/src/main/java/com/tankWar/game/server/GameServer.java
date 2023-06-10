package com.tankWar.game.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.game.msg.InitMsg;
import com.tankWar.game.msg.MessageType;
import com.tankWar.game.msg.OverMsg;
import com.tankWar.game.msg.ResetMsg;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;

// v1 服务端只提供消息的转发，不负责统一的状态管理
// v2 服务端能够记录状态，并且定时发送状态同步消息

public class GameServer {
    // 记录游戏
    int totalGameNum;
    int curGameNum = 0;

    // 记录玩家数量
    // 考虑有人会退出游戏, 保持id的一致性
//    int totalPlayerNum;
    Vector<Integer> totalPlayer;
    // 记录剩余玩家
    Vector<Integer> restPlayer;

    // 记录分数
    int[] scores;
    // 用于终结游戏
    boolean isOver = false;

    // 服务端Socket
    ServerSocket serverSocket;
    // 服务端端口号
    int port;
    // 客户端Socket列表
    Socket[] sockets;
    // 输入输出数据流
    DataOutputStream[] out;

    // 用于处理json绑定
    ObjectMapper mapper = new ObjectMapper();

    // 在游戏房间中构造，包含玩家个数
    public GameServer(int num, int port) {
        // 初始化游戏局数
        this.totalGameNum = 2;
        this.curGameNum = 1;
        this.port=port;


        // num为游戏中的玩家数量
        // 初始化玩家 和 积分数
        totalPlayer =  new Vector<>(num);
        for(int i=0; i<num; i++)
            totalPlayer.add(i);
        restPlayer = new Vector<>(totalPlayer);

        scores = new int[num];
        Arrays.fill(scores, 0);

        // 初始化列表
        sockets = new Socket[num];
        out = new DataOutputStream[num];
    }

    public void closeServer(){
        try {
            serverSocket.close();
            System.out.println("[info] Socket关闭");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 运行函数
    public void run()  {
        // 1.建立TCP连接
        try {
            serverSocket = new ServerSocket(this.port);
            ServerPrompt.RunSuccess.print();

            // 建立TCP连接
            for(int i: totalPlayer) {
//            System.out.println("正在等待连接");
                sockets[i] = serverSocket.accept();
                // 数据流绑定客户端Socket
                out[i] = new DataOutputStream(sockets[i].getOutputStream());
                System.out.println("[info] 服务端已连接" + (i+1));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        // 2.获取并广播初始化信息
        try {
            this.sendInitMsg();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. 创建多线程连接业务处理每个客户端连接
        for(int i: totalPlayer) {
            ReceiveThread t = new ReceiveThread(i);
            t.start();
        }
    }

    // 发送初始化信息
    void sendInitMsg() throws IOException{
        // 添加地图信息
        int mapId = 0;

        // 广播发送所有坦克初始化信息
        for (int i: totalPlayer) {
            // 配置消息的基本信息
            InitMsg message = new InitMsg(i, mapId, totalPlayer.size(), totalGameNum);
            // 转换成JSON格式并发送
            String jsonMsg = mapper.writeValueAsString(message);
            out[i].writeUTF(jsonMsg);
        }
        ServerPrompt.AllSend.print();
    }

    // 发送重置信息
    void sendResetMsg() throws IOException{
        // 添加地图信息
        int mapId = 0;

        // 重置消息对于所有人都是相同的
        ResetMsg message = new ResetMsg(mapId, totalPlayer.size(), curGameNum);
        // 转换成JSON格式并发送
        String jsonMsg = mapper.writeValueAsString(message);
        broadcast(-1, jsonMsg);
        ServerPrompt.AllSend.print();
    }

    // 发送结束消息
    void sendOverMsg() throws IOException {
        OverMsg msg = new OverMsg(scores);
        String jsonMsg = mapper.writeValueAsString(msg);
        broadcast(-1, jsonMsg);
    }

    // 广播状态
    void broadcast(int id, String msg) throws IOException {
        for(int i: totalPlayer) {
            if(i == id)
                continue;
            out[i].writeUTF(msg);
        }

        ServerPrompt.BroadcastSuccess.print();
    }

    // 用来处理接收客户端信息线程
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

        void handleDeadMsg() throws IOException {
            // 1.记录死亡信息
            restPlayer.removeElement(id);
            if(restPlayer.size()!=1)
                return;

            // 2. 当只剩下一位玩家则重置游戏
            int winnerId = restPlayer.get(0);
            // 重置死亡信息
            restPlayer.clear();
            restPlayer.addAll(totalPlayer);

            // 3. 加分
            scores[winnerId]++ ;

            // 4. 判断是否是最后场游戏
            if(curGameNum < totalGameNum) {
                curGameNum++;
                sendResetMsg();
                return;
            }

            // 5. 结束游戏
            isOver = true;
            sendOverMsg();
            ServerPrompt.GameOver.print();
        }

        @Override
        public void run() {
            // 循环接收消息
            while (!isOver) {
                // 1. socket接收到JSON消息
                try {
                    String msgStr = in.readUTF();
//                    System.out.println("来自客户端的消息: " + msgStr);

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
                } catch(SocketException e) {
                    // todo 处理断连情况
                    e.printStackTrace();
                    break;
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
