package com.tankWar.game.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.game.msg.InitMsg;
import com.tankWar.game.msg.MessageType;
import com.tankWar.game.msg.OverMsg;
import com.tankWar.game.msg.TankInfo;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;

// v1 服务端只提供消息的转发，不负责统一的状态管理
// v2 服务端能够记录状态，并且定时发送状态同步消息

public class GameServer {
    // 记录玩家数量
    int player_num;
    // 记录游戏
    int game_num;
    // 记录剩余玩家
    Vector<Integer> restPlayer;
    int[] scores;
    // 用于终结游戏
    boolean isOver = false;

    // 服务端Socket
//    ServerSocket serverSocket;
    // 服务端端口号
    int port;
    // 客户端Socket列表
    Socket[] sockets;
    // 输入输出数据流
    DataOutputStream[] out;

    // 用于处理json绑定
    ObjectMapper mapper = new ObjectMapper();

    // 在游戏房间中构造，包含玩家个数
    public GameServer(Socket[] sockets) {

        // num为游戏中的玩家数量
        this.player_num = sockets.length;
        // 初始化游戏局数
        this.game_num = 1;
//        this.port=port;

        // 初始化剩余玩家 和 积分数
        scores = new int[this.player_num];
        Arrays.fill(scores, 0);
        restPlayer =  new Vector<>(this.player_num);
        for(int i=0; i<this.player_num; i++)
            restPlayer.add(i);

        // 初始化列表
//        sockets = new Socket[num];
        this.sockets = sockets;
        out = new DataOutputStream[this.player_num];
    }

//    public void closeServer(){
//        try {
//            serverSocket.close();
//            System.out.println("[info] Socket关闭");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    // 运行函数
    public void run()  {
        // 1.建立TCP连接
        try {
//            serverSocket = new ServerSocket(this.port);
            ServerPrompt.RunSuccess.print();

            // 建立TCP连接
            for(int i = 0; i< player_num; i++) {
//            System.out.println("正在等待连接");
//                sockets[i] = serverSocket.accept();
                // 数据流绑定客户端Socket
                out[i] = new DataOutputStream(sockets[i].getOutputStream());
                System.out.println("[info] 服务端已连接" + (i+1));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        // 2.获取并广播初始化信息
        try {
            Thread.sleep(2000);
            this.sendInitMsg();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 3. 创建多线程连接业务处理每个客户端连接
        for(int i = 0; i< player_num; i++) {
            ReceiveThread t = new ReceiveThread(i);
            t.start();
        }
    }

    // 发送初始化信息
    void sendInitMsg() throws IOException{
        // todo 添加地图信息
        int mapId = 0;

        // todo 添加坦克信息
        TankInfo[] tanks = new TankInfo[2];
        tanks[0] = new TankInfo(0, 100, 100);
        tanks[1] = new TankInfo(1, 200, 200);
        System.out.println("[info] player num:"+player_num);
        // 广播发送所有坦克初始化信息
        for (int i = 0; i < player_num; i++) {
            System.out.println("[info] player"+i+"socket:"+sockets[i]);
            // 配置消息的基本信息
            InitMsg message = new InitMsg(i, mapId, tanks);
            // 转换成JSON格式并发送
            String jsonMsg = mapper.writeValueAsString(message);
            out[i].writeUTF(jsonMsg);
        }
        ServerPrompt.AllSend.print();
    }

    // 发送结束消息
    void sendOverMsg() throws IOException {
        System.out.println("game over!!!!!!!!!");
        OverMsg msg = new OverMsg(scores);
        String jsonMsg = mapper.writeValueAsString(msg);
        broadcast(-1, jsonMsg);
    }

    // 广播状态
    void broadcast(int id, String msg) throws IOException {
        for(int i = 0; i< player_num; i++) {
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
//                System.out.println(in.available());
//                int availableBytes = in.available();
//                System.out.println("aaaaaaaaaa"+availableBytes);
//                // 跳过可读取的字节数
//                in.skipBytes(availableBytes);
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
            for(int i=0; i<player_num; i++)
                restPlayer.add(i);

            // 3. 加分
            scores[winnerId]++ ;

            // 4. 判断是否是最后场游戏
            game_num--;
            if(game_num != 0) {
                sendInitMsg();
                return;
            }

            // 5. 结束游戏
            isOver = true;
            sendOverMsg();
            ServerPrompt.GameOver.print();
        }
        int count = 0;
        @Override
        public void run() {
            // 循环接收消息
            while (!isOver) {
                // 1. socket接收到JSON消息
                try {
                    System.out.println(in.available());
                    String msgStr = in.readUTF();
                    System.out.println("来自客户端的消息: " + msgStr+"  "+ count);
                    count++;

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
