package com.tankWar.game.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.tankWar.communication.msg.*;
import com.tankWar.game.entity.Direction;
import com.tankWar.game.msg.*;
import com.tankWar.communication.Communicate;

import java.net.*;

public class GameClient {
    int id = -1;
    // 客户端Socket
    Socket clientSocket;
    // 用于处理JSON绑定
    ObjectMapper mapper = new ObjectMapper();

    public GameClient() {

    }

    public GameClient(int id) {
        this.id = id;
    }

    public void setSocket(Socket socket) {
        this.clientSocket = socket;
    }

    // 发送消息和接收 (调用Communicate中的函数)
    void send(String msg) {
        Communicate.send(clientSocket, msg);
    }

    String receive() {
        return Communicate.receive(clientSocket);
    }

    // 发送移动消息
    public void sendMoveMsg(Direction dir, double x, double y) {
        MoveMsg moveMsg = new MoveMsg(id, dir, x, y);
        try {
            String msg = mapper.writeValueAsString(moveMsg);
            this.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 发送发射消息
    public void sendShootMsg(Direction dir, double x, double y) {
        ShootMsg shootMsg = new ShootMsg(id, dir, x, y);
        try {
            String msg = mapper.writeValueAsString(shootMsg);
            this.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 发送自己死亡的消息
    public void sendDeadMsg() {
        DeadMsg deadMsg = new DeadMsg(this.id);
        try {
            String msg = mapper.writeValueAsString(deadMsg);
            this.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 接收消息 服务端返回的状态 (需要额外开辟一个线程)
    public Message receiveStatusMsg() {
        // 1.读取Json数据
        String msg = receive();
        if(msg.isEmpty()){
            return null;
        }

        // 2. todo 校验JSON格式
//        System.out.println(msg);

        // 3. 解析并返回消息
        try {
            JsonNode jsonMsg = mapper.readTree(msg);

            // [Warn] 直接使用toString输出内容会包括双引号
//            System.out.println("msg:" +  jsonMsg.get("type").toString());

            MessageType type = MessageType.valueOf(jsonMsg.get("type").asText());

            // 3.1 解析并返回消息
            switch (type) {
                case Move -> {
                    return mapper.readValue(msg, MoveMsg.class);
                }
                case Shoot -> {
                    return mapper.readValue(msg, ShootMsg.class);
                }
                case Init->{
                    // InitMsg会给GameClient分配ID
                    InitMsg initMsg = mapper.readValue(msg, InitMsg.class);
                    this.id = initMsg.getId();
                    return initMsg;
//                    return mapper.readValue(msg, InitMsg.class);
                }
                case Reset -> {
                    return mapper.readValue(msg, ResetMsg.class);
                }
                case Over->{
                    return mapper.readValue(msg, OverMsg.class);
                }
                default -> {
                    System.out.println("[error] 接收消息异常!");
                    return  null;
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getId() {
        return id;
    }
}


