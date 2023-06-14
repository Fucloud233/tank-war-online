package com.tankWar.lobby;

import com.tankWar.server.Config;
import java.io.*;
import java.net.Socket;

// 定义客户端与服务端通信的接口
public class Communicate {
    // 接口标准
    // 1. 定长数字(长度为3, 可以在服务端定义)作为头部 说明大小
    // 2. 然后根据该大小读取后面的内容
    static final int HeaderLen = Config.getHeaderLength();

    public static void send(Socket socket, String text) {
        try {
            // 1. 打开字节输出流
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // 2. 计算长度并生成输出文本
            int size = text.getBytes().length;
            String outputText = toText(size) + text;

            // 3. 输出字节
            out.write(outputText.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String receive(Socket socket) {
        if(socket==null)
            return null;

        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // 1. 读取头部内容
            byte[] headerBytes = new byte[HeaderLen];
            int len = in.read(headerBytes, 0, HeaderLen);
            if (len == -1)
                return null;

            // 2. 读取消息体内的内容
            int size = toNum(new String(headerBytes));
            int curLen = 0;
            byte[] bodyBytes = new byte[size];

            // 循环读取
            while(size > 0) {
                int temp = in.read(bodyBytes, curLen, size);
                size -= temp;
                curLen += temp;
            }

            // 3. 返回消息
            return new String(bodyBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String toText(int num) {
        String format = "%0" + HeaderLen + "d";

        return String.format(format, num);
    }

    private static int toNum(String text) {
        return Integer.parseInt(text);
    }
}
