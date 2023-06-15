package com.game.fucloud;

import com.tankWar.game.client.GameClient;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import com.tankWar.communication.msg.Message;
import com.tankWar.game.entity.Direction;
import org.junit.Test;

public class GameClientTest {
    static Random ra = new Random();


    // 运行的那个客户端测试
    @Test
    public void runClient() {
        GameClient client = new GameClient(0);

//        // 建立连接
//        try {
//            System.out.println("[error] 正在连接");
//            client.connect();
//            System.out.println("[info] Connection Success!");
//        }
//        catch(TimeoutException e) {
//            System.out.println("连接超时");
//        }
//        catch(IOException e) {
//            System.out.println("[error] Connection failed!");
//        }

        // 客户端向服务端发送消息
        client.sendMoveMsg(Direction.LEFT, 0, 0);
    }

    // 多个客户端测试
    @Test
    public void testRunClients() {
        int size = 4;
        Utils.runGameSever(size);

        GameClient[] clients = new GameClient[size];
        for(int i=0; i<size; i++)
            clients[i] = new GameClient(i);

//        // 建立连接
//        try {
//            System.out.println("[info] 正在连接");
//            for(int i=0; i<size; i++) {
//                clients[i].connect();
////                Thread.sleep(100);
//            }
//            System.out.println("Connection Success!");
//        }
//        catch(TimeoutException e) {
//            System.out.println("[error] 连接超时");
//        }
//        catch(IOException e) {
//            System.out.println("[error] Connection failed!");
//        }

        // 测试内容：随机选择客户端想服务端发送消息
        for(int i=0; i<10; i++) {
            int id = ra.nextInt(0, 4);
            clients[id].sendMoveMsg(Direction.LEFT, 0, 0);
        }
    }

    @Test
    public void runRecvClients() {
        // 启动服务端
        Utils.runGameSever(2);

        GameClient clientA = new GameClient(0), clientB = new GameClient(1);

//        // 建立连接
//        try {
//            System.out.println("[info] 正在连接");
//            clientA.connect();
//            clientB.connect();
//            System.out.println("[info] Connection Success!");
//        }
//        catch(TimeoutException e) {
//            System.out.println("[error] 连接超时");
//        }
//        catch(IOException e) {
//            System.out.println("[error] Connection failed!");
//            e.printStackTrace();
//            return;
//        }
        // 测试内容：随机选择客户端想服务端发送消息
        clientA.sendMoveMsg(Direction.LEFT, 0, 0);

        Message msg = clientB.receiveStatusMsg();
        if (msg != null) {
            System.out.println("[info] 客户端B接收: " + msg.getType());
        }
    }
}


