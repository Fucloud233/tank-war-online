package com.game.fucloud;

import com.tankWar.game.client.GameClient;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import com.tankWar.game.server.GameServer;
import org.junit.BeforeClass;
import org.junit.Test;

public class GameClientTest {
    static int size = 4;

    static Random ra = new Random();

    // 启动服务端
    @BeforeClass
    public static void runServer() throws IOException {
        Thread t = new Thread(()->{
            GameServer server = new GameServer(size);
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        t.start();

    }

    // 运行的那个客户端测试
//    @Test
    public void runClient() throws IOException {
        GameClient client = new GameClient();

        // 建立连接
        try {
            System.out.println("正在连接");
            client.connect();
            System.out.println("Connection Success!");
        }
        catch(TimeoutException e) {
            System.out.println("连接超时");
        }
        catch(IOException e) {
            System.out.println("Connection failed!");
        }

        // 客户端向服务端发送消息
//        client.send();
    }


    // 多个客户端测试
    @Test
    public void runClients() throws IOException {
        GameClient[] clients = new GameClient[size];
        for(int i=0; i<size; i++)
            clients[i] = new GameClient();

        // 建立连接
        try {
            System.out.println("正在连接");
            for(int i=0; i<size; i++) {
                clients[i].connect();
//                Thread.sleep(100);
            }
            System.out.println("Connection Success!");
        }
        catch(TimeoutException e) {
            System.out.println("连接超时");
        }
        catch(IOException e) {
            System.out.println("Connection failed!");
        }

        // 测试内容：随机选择客户端想服务端发送消息
        for(int i=0; i<10; i++) {
            int id = ra.nextInt(0, 4);
//            clients[id].send(new Message(id, Command.CENTER));
        }
    }
}


