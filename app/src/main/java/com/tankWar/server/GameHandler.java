package com.tankWar.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.game.msg.InitMsg;
import com.tankWar.game.msg.MessageType;
import com.tankWar.game.msg.OverMsg;
import com.tankWar.game.msg.ResetMsg;

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.Vector;

// v1 服务端只提供消息的转发，不负责统一的状态管理
// v2 服务端能够记录状态，并且定时发送状态同步消息

public class GameHandler extends Handler{
    // 用于处理json绑定
    ObjectMapper mapper = new ObjectMapper();
    Game game = null;

    // 构造函数
    GameHandler(SocketChannel socket, Room room) {
        super(socket);
        this.game = room.game;
    }

    // 发送初始化信息
    void sendInitMsg() throws IOException{
        // 添加地图信息
        int mapId = 0;

        Vector<SocketChannel> sockets  = game.getAllSockets();
        int id = 0;
        // 广播发送所有坦克初始化信息
        for (SocketChannel socket: sockets) {
            // 配置消息的基本信息
            InitMsg message = new InitMsg(id, mapId, sockets.size(), game.getTotalGameNum());
            // 转换成JSON格式并发送
            String jsonMsg = mapper.writeValueAsString(message);
            // 发送
            sendMsg(socket, jsonMsg);

            id++;
        }
        ServerPrompt.AllSend.print();
    }

    // 发送重置信息
    void sendResetMsg() throws IOException{
        // 添加地图信息
        int mapId = 0;

        // 重置消息对于所有人都是相同的
        ResetMsg message = new ResetMsg(mapId, game.getTotalPlayerNum(), game.getCurGameNum());
        // 转换成JSON格式并发送
        String jsonMsg = mapper.writeValueAsString(message);
        sendAll(jsonMsg);
        ServerPrompt.AllSend.print();
    }

    // 发送结束消息
    void sendOverMsg() throws IOException {
        OverMsg msg = new OverMsg(game.getScores());
        String jsonMsg = mapper.writeValueAsString(msg);
        sendAll(jsonMsg);
    }

    // 广播状态
    void sendAll(String msg) {
        for (SocketChannel socket: game.getAllSockets())  {
            sendMsg(socket, msg);
        }

        ServerPrompt.BroadcastSuccess.print();
    }

    // 广播状态(除了当前Sockets)
    void sendAllWithoutMe(String msg) {
        for (SocketChannel socket: game.getAllSockets())  {
            if(socket != curSocket)
                continue;
            sendMsg(socket, msg);
        }

        ServerPrompt.BroadcastSuccess.print();
    }

    void handleDeadMsg(int id) throws IOException {
        // 1. 记录死亡信息
        DeadStatus flag = game.dead(id);

        // 2. 根据死亡结果判断要发送的消息类型
        if(flag.shouldReset()) {
            sendResetMsg();
        } else if (flag.shouldOver()) {
            sendOverMsg();
            ServerPrompt.GameOver.print();
        }
    }

    @Override
    public void receive() {
        // 发送初始消息
//        sendInitMsg();

        // 1. socket接收到JSON消息
        try {
            String msgStr = this.receiveMsg();
//            System.out.println("来自客户端的消息: " + id  + ' ' + msgStr+"  ");

            // 2. 进行验证
            MessageType type = null;
            int id = -1;
            try {
                JsonNode json = mapper.readTree(msgStr);
                type = MessageType.valueOf(json.get("type").asText());
                id = json.get("id").asInt();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            // 如果是死亡消息 则需要单独处理
            if(type == MessageType.Dead) {
                this.handleDeadMsg(id);
            }

            // 3. socket广播消息
            sendAllWithoutMe(msgStr);
        } catch(SocketException e) {
            // todo 处理断连情况
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
