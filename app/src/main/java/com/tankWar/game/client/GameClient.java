package com.tankWar.game.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.game.entity.Direction;
import com.tankWar.game.msg.*;
import com.tankWar.game.server.Config;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeoutException;

public class GameClient {
    int id = -1;

    Socket clientSocket;

    DataOutputStream out;
    DataInputStream in;

    // 用于处理JSON绑定
    ObjectMapper mapper = new ObjectMapper();

    public GameClient() {

    }

    public GameClient(int id)  {
        this.id = id;
    }

    // 初始化连接 连接正常就不会抛出异常
    public void connect() throws IOException, TimeoutException {
        clientSocket = new Socket(Config.ip, Config.port);
        clientSocket.setSoTimeout(1000);

        // 初始化输入输出端口
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    // 发送消息
    void send(String msg)  {
        try {
            out.writeUTF(msg);
        }
        catch( IOException e ){
            System.out.printf("[Error] 客户端%d发送失败!\n", id);
        }
    }

    String receive() {
        try {
            return in.readUTF();
        } catch( IOException e) {
//            System.out.printf("[Error] 客户端%d接收失败!\n", id);
            return "";
        }
    }

    // 发送移动消息
    public void sendMoveMsg(Direction dir) {
        MoveMsg moveMsg = new MoveMsg(id, dir);
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
            String msg  = mapper.writeValueAsString(shootMsg);
            this.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 发送自己死亡的消息
    public void sendDeadMsg() {
        DeadMsg deadMsg = new DeadMsg(this.id);
        try {
            String msg  = mapper.writeValueAsString(deadMsg);
            this.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 接收消息 服务端返回的状态 (需要额外开辟一个线程)
    public Message receiveStatusMsg() {
        String msg = receive();

        if(msg.isEmpty()){
            return null;
        }

        // 1.读取Json数据
        // todo 校验JSON格式

        // 4.1 解析消息数据类型
        MessageType type = null;
        try {
            JsonNode jsonMsg = mapper.readTree(msg);

            // [Warn] 直接使用toString输出内容会包括双引号
//            System.out.println("msg:" +  jsonMsg.get("type").toString());

            type = MessageType.valueOf(jsonMsg.get("type").asText());

            // 3.1 解析并返回消息
            switch(type) {
                case Move -> {
                    return mapper.readValue(msg, MoveMsg.class);
                }
                case Shoot-> {
                    return mapper.readValue(msg, ShootMsg.class);
                }
                case Init->{
                    return mapper.readValue(msg, InitMsg.class);
                }
                case Over->{
                    return mapper.readValue(msg, OverMsg.class);
                }
                default -> {
                    System.out.println("接收消息异常!");
                    return  null;
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public InitMsg receiveInitMsg() {
        String msg = receive();
        if(msg.isEmpty())
            return null;

        try {
            InitMsg initMsg = mapper.readValue(msg, InitMsg.class);
            this.id = initMsg.getId();
            return initMsg;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}


