package com.tankWar.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.communication.msg.InitMsg;
import com.tankWar.communication.msg.MessageType;
import com.tankWar.communication.msg.OverMsg;
import com.tankWar.communication.msg.ResetMsg;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.Vector;

// v1 服务端只提供消息的转发，不负责统一的状态管理
// v2 服务端能够记录状态，并且定时发送状态同步消息

public class GameHandler extends Handler{
    // 用于处理json绑定
    ObjectMapper mapper = new ObjectMapper();

    // 用于记录游戏信息
    Game game;
    Room room;

    // 构造函数
    GameHandler(SocketChannel socket, Room room) {
        super(socket);
        this.room = room;
        this.game = room.game;
    }

    // 发送初始化信息 (需要在Room's startGame调用 所以public)
    public void sendInitMsg() throws IOException{
        // 添加地图信息
        int mapId = game.getMapId();

        Vector<SocketChannel> sockets  = game.getAllSockets();
        int id = 0;
        // 广播发送所有坦克初始化信息
        for (SocketChannel socket: sockets) {
            // 配置消息的基本信息
            InitMsg message = new InitMsg(id, mapId, sockets.size(), game.getTotalGameNum());
            // 转换成JSON格式并发送
            String jsonMsg = mapper.writeValueAsString(message);
            // 发送
            send(socket, jsonMsg);

            id++;
        }
        ServerPrompt.AllSend.print();
    }

    // 发送重置信息
    void sendResetMsg() throws IOException{
        // 添加地图信息
        int mapId = game.getMapId();

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
        ServerPrompt.GameOver.print();
        room.endGame();
    }

    // 广播状态
    void sendAll(String msg) {
        for (SocketChannel socket: game.getAllSockets())  {
            send(socket, msg);
        }
        ServerPrompt.BroadcastSuccess.print();
    }

    // 广播状态(除了当前Sockets)
    void sendAllWithoutMe(String msg) {
        for (SocketChannel socket: game.getAllSockets())  {
            if(socket == curSocket)
                continue;
            send(socket, msg);
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
        }
    }

    @Override
    public void handle() {
        try {
            // 1. socket接收到JSON消息
            String msgStr = this.receive();
            // 2. 进行验证
            JsonNode json = mapper.readTree(msgStr);
            MessageType type = MessageType.valueOf(json.get("type").asText());
            int id = json.get("id").asInt();

            // 如果是死亡消息 则需要单独处理
            if(type == MessageType.Dead) {
                this.handleDeadMsg(id);
                return;
            }
            // 3. socket广播消息
            sendAllWithoutMe(msgStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch(IOException e) {
            // todo 处理断连情况
            e.printStackTrace();
        }
    }
}
