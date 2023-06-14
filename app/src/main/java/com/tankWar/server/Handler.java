package com.tankWar.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Handler {
    SocketChannel curSocket = null;

    Handler(SocketChannel socket) {
        this.curSocket = socket;
    }

    // 接收消息的接口
    protected String receiveMsg() {
        // 1. 读取消息
        String text;
        try {
            // 1. 读取消息
            ByteBuffer buffer = ByteBuffer.allocate(128);
            int len = curSocket.read(buffer);

            // 2. 判断消息是否为空
            if (len == -1) {
                System.out.println("[warn] 接收到空消息");
                return null;
            }

            // 3. 得到读取到的消息
            buffer.rewind();
            byte[] bytes = new byte[len];
            buffer.get(bytes);
            text = new String(bytes, 2, len-2);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return text;
    }

    // 发送消息的接口
    void sendMsg(String text) {
        try {
            curSocket.write(ByteBuffer.wrap(text.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendMsg(SocketChannel socket, String text) {
        try {
            socket.write(ByteBuffer.wrap(text.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public abstract void receive();
}