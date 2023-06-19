package com.tankWar.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Handler {
    static final int HeaderLen = Config.getHeaderLength();

    SocketChannel curSocket;

    Handler(SocketChannel socket) {
        this.curSocket = socket;
    }

    // 接收消息的接口
    protected String receive() throws IOException {
            // 1. 读取头部消息
            ByteBuffer headerBuffer = ByteBuffer.allocate(HeaderLen);
            int headerLen = curSocket.read(headerBuffer);
            if(headerLen==-1)
                return null;

            byte[] headerBytes = new byte[HeaderLen];
            headerBuffer.rewind();
            headerBuffer.get(headerBytes, 0, HeaderLen);

            // 2. 读取消息体消息
            int size = toNum(new String(headerBytes));
            headerBuffer = ByteBuffer.allocate(size);

            int restSize = size;
            // 循环读取
            while(restSize>0) {
                int temp = curSocket.read(headerBuffer);
                restSize -= temp;
            }

            byte[] bodyBytes = new byte[size];
            headerBuffer.rewind();
            headerBuffer.get(bodyBytes, 0, size);

            return new String(bodyBytes);
    }

    void send(SocketChannel socket, String text) {
        try {
            // 1. 获得消息长度
            // [important] 注意这里要用字节长度 不能用字符长度
            // Java中一个汉字1字符, 2字节
            int size = text.getBytes().length;
//            System.out.println("len: " +size + " " + text);

            // 2. 将消息长度插入头部
            String outputText = toText(size) + text;
            // 3. 发送消息
            socket.write(ByteBuffer.wrap(outputText.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 发送消息的接口
    void send(String text) {
        send(curSocket, text);
    }

    private static String toText(int num) {
        String format = "%0" + HeaderLen + "d";

        return String.format(format, num);
    }

    private static int toNum(String text) {
        try {
            return Integer.parseInt(text);
        } catch(NumberFormatException e) {
            return 0;
        }
    }

    public abstract void handle() throws IOException;
}