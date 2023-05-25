package com.tankWar.game.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientAppUDP {
    public static void main(String[] args)throws Exception{
        // 定义一个字符串：要发送的内容
        String message = "约吗";
        // 字符串转字节数组
        byte[] buf = message.getBytes();
        // 创建数据包对象
        DatagramPacket dp = new DatagramPacket(buf,buf.length,
                InetAddress.getLocalHost(),8080);
        // 创建发送端的发送对象
        DatagramSocket ds = new DatagramSocket(8888);
        // 发送数据包
        ds.send(dp);
        // 关闭发送对象释放端口号
        ds.close();
    }
}
