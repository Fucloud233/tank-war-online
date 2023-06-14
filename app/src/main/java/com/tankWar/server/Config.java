package com.tankWar.server;

public class Config {
    // 定义 输入输出流字节 头部长度
    static int HeaderLength = 3;

    // 服务端配置信息
    public final static String ip = "127.0.0.1";
    public final static int port = 8080;

    // 数据库配置信息
    // https://www.jianshu.com/p/d7b9c468f20dg
    static String dbIp = "47.120.5.208";
    static int dbPort = 19998;
    static String dbName = "javaproject";
    static String dbUserName = "java";
    static String dbPassword = "Wu_123456";

    public static String getDbIp() {
        return dbIp;
    }

    public static int getDbPort() {
        return dbPort;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static String getDbName() {
        return dbName;
    }

    public static String getDbUserName() {
        return dbUserName;
    }

    public static String getDbURL() {
        return "jdbc:mysql://" + dbIp + ":" + dbPort + '/' + dbName;
    }

    public static int getHeaderLength() {
        return HeaderLength;
    }

}
