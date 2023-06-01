package com.tankWar.game.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.game.client.msg.*;
import com.tankWar.game.entity.Direction;
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

    // 发送移动消息
    public void sendMove(Direction dir) {
        MoveMessage moveMessage = new MoveMessage(id, dir);
        try {
            String msg = mapper.writeValueAsString(moveMessage);
            this.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 发送发射消息
    public void sendShoot(Direction dir, int x, int y) {
        ShootMessage shootMessage = new ShootMessage(id, dir, x, y);
        try {
            String msg  = mapper.writeValueAsString(shootMessage);
            this.send(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
//            e.printStackTrace();
//            System.out.printf("[Error] 客户端%d接收失败!\n", id);
            return "";
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

            // 3.1 解析并返回移动消息
            if (type == MessageType.Move) {
                return mapper.readValue(msg, MoveMessage.class);
            }
            // 3.2 解析返回发射消息
            else if (type == MessageType.Shoot) {
                return mapper.readValue(msg, ShootMessage.class);
            }
            else {
                System.out.println("接收消息异常!");
                return  null;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public InitMessage receiveInitMsg() {
        String msg = receive();
        if(msg.isEmpty())
            return null;

//        JSONObject jsonMsg = JSONObject.parseObject(msg);
        try {
            InitMessage initMsg = mapper.readValue(msg, InitMessage.class);
            this.id = initMsg.getId();
            return initMsg;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}


