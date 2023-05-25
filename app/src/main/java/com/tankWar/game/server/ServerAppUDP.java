package com.tankWar.game.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerAppUDP {
    public static void main(String[] args)throws Exception{
        // 创建接收对象DatagramSocket
        DatagramSocket ds = new DatagramSocket(8080);
        // 创建字节数组用来存储接收接收到的内容
        byte[] buf = new byte[1024];
        // 创建数据包对象
        DatagramPacket dp = new DatagramPacket(buf,buf.length);
        // 接收数据包
        ds.receive(dp);

        // 获得实际接收到的字节个数
        int len = dp.getLength();
        System.out.println("len = " + len);
        // 将字节数组的内容转换为字符串输出
        System.out.println(new String(buf,0,len));

        // 获得发送端的ip地址
        String sendIp = dp.getAddress().getHostAddress();
        // 获得发送端的端口号
        int port  = dp.getPort();
        System.out.println(sendIp);
        System.out.println(port);

        // 关闭Socket对象
        ds.close();
    }
}
