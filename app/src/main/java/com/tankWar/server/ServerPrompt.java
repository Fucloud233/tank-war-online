package com.tankWar.server;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerPrompt {
    // 提示信息
    public static void infoServerRunning(int port) {
        info("服务器端口运行中 (Port: " + port +")");
    }

    public static void infoClientConnectSuccess(InetAddress address, int port) {
        info( "客户端 " + address.getHostAddress() + ":" + port + " 连接成功");
    }

    public static void infoRegisterSuccess(String name) {
        info("User " + name + " 注册成功");
    }

    public static void infoLoginSuccess(String account, int curUserNumber) {
        info("用户 " + account + " 登录成功 (当前人数: " + curUserNumber + ")");

    }

    public static void infoCreateRoom(String userName, String roomName) {
        info(userName + " 创建房间(Name: " + roomName + ")");
    }

    public static void infoHostExitRoom(String roomName) {
        info("房主从房间(Name: " + roomName + ")离开, 该房间解散");
    }

    public static void infoGameStart(String roomName) {
        info("房间 (Name: " + roomName + ") 开始游戏");
    }

    public static void infoGameOver(String roomName) {
        info("房间 (Name: " + roomName + ") 游戏结束");
    }

    public static void infoOnlinePlayerNumber(int num) {
        info("当前在线人数: " + num);
    }

    public static void infoAllSendSuccess() {
        info("客户端消息全部发送成功");
    }

    public static void infoBroadcastSuccess() {
        info("广播成功");
    }

    // 警告信息
    public static void warnLoginFail(String account) {
        warn("用户 "+ account + " 登陆失败");
    }

    // 提示 用户名/账号 注册失败
    public static void warnRegisterFail(String account) {
        warn(account + " Register fail!");
    }

    public static void warnPlayerLeave(String playerName) {
        warn("用户 " + playerName + " 断开连接");
    }

    public static void warnSelectError() {
        warn("Select error!");
    }

    // 错误信息
    public static void errorPortUsed(int port) {
        error(port + "端口使用中, 请重新设置端口");
    }

    public static void errorServerStartFail() {
        error("Can not start server.");
    }


    // 基础函数
    private static void info(String text) {
        System.out.println("[info] " + getCurTime() + " " + text);
    }

    private static void warn(String text) {
        System.out.println("[warn] " + getCurTime() + " " + text);
    }

    private static void error(String text) {
        System.out.println("[error] " + getCurTime() + " " + text);
    }

    // 获得当前时间
    private static String getCurTime() {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
        return format.format(new Date());
    }
}


