package com.tankWar.game.client;

import com.alibaba.fastjson.JSONObject;
import com.tankWar.game.entity.Direction;
import com.tankWar.game.server.Config;
import com.tankWar.game.client.msg.MessageType;
import com.tankWar.game.client.msg.Message;
import com.tankWar.game.client.msg.MoveMessage;
import com.tankWar.game.client.msg.ShootMessage;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeoutException;

public class GameClient {
    int id;

    Socket clientSocket;

    DataOutputStream out;
    DataInputStream in;

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
        JSONObject msg = new JSONObject();
        msg.put("type", MessageType.Move);
        msg.put("id", id);
        msg.put("dir", dir);

        this.send(msg);
    }

    // 发送发射消息
    public void sendShoot(Direction dir, int x, int y) {
        JSONObject msg =  new JSONObject();
        msg.put("type", MessageType.Shoot);
        msg.put("id", id);
        msg.put("x", x);
        msg.put("y", y);

        this.send(msg);
//        return result;
    }

    // 发送消息
    void send(JSONObject obj)  {
        try {
            out.writeUTF(obj.toString());
        }
        catch( IOException e ){
            System.out.printf("[Error] 客户端%d发送失败!\n", id);
        }
    }

    String receive() {
        try {
            String msg = in.readUTF();
            return msg;
        } catch( IOException e) {
//            e.printStackTrace();
            System.out.printf("[Error] 客户端%d接收失败!\n", id);
            return "";
        }
    }

    // 接收消息 服务端返回的状态 (需要额外开辟一个线程)
    public Message receiveMessage() {
        String msg = receive();
        if(msg.isEmpty()){
            return null;
        }

        // 1.读取Json数据
        // todo 校验JSON格式
        JSONObject jsonMsg = JSONObject.parseObject(msg);

        // 4.1 解析消息数据类型
        MessageType type = MessageType.valueOf(jsonMsg.getString("type"));
        int id = jsonMsg.getInteger("id");

        // 3.1 解析并返回移动消息
        if (type == MessageType.Move) {
            Direction dir = Direction.valueOf(jsonMsg.getString("dir"));
            return new MoveMessage(id, type, dir);
        }
        // 3.2 解析返回发射消息
        else if (type == MessageType.Shoot) {
            Direction dir = Direction.valueOf(jsonMsg.getString("dir"));
            int x = jsonMsg.getInteger("x");
            int y = jsonMsg.getInteger("y");

            return new ShootMessage(id, type, dir, x, y);
        }

        return null;
    }
}


