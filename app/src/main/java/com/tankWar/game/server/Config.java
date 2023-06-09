package com.tankWar.game.server;

public class Config {
    public final static String ip = "127.0.0.1";
    public final static int port = 8080;

    // 数据库配置信息
    // https://www.jianshu.com/p/d7b9c468f20dg
    private static String dbIp = "47.120.5.208";
    private static int dbPort = 19998;
    private static String dbName = "javaproject";
    private static String dbUserName = "java";
    private static String dbPassword = "Wu_123456";

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

}
