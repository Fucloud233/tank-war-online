package com.tankWar.server;

import java.net.Socket;

// 对User的信息进行扩展
// 使其能够包括Socket和Status信息
public class UserInfo extends User {
    UserStatus status = UserStatus.Null;
    Socket socket = null;

    public UserInfo() {
        super(null, null, null);
    }

    public UserInfo(String nickName, String account, Socket socket, UserStatus status) {
        super(nickName, account, null);
        this.status = status;
        this.socket = socket;
    }

    public UserInfo(String nickName, String account, Socket socket) {
        super(nickName, account, null);
        this.socket = socket;
    }

    public UserInfo(User user, Socket socket, UserStatus status) {
        super(user.nickName, user.account, user.password);
        this.status = status;
        this.socket = socket;
    }

    public UserInfo(User user, Socket socket) {
        super(user.nickName, user.account, user.password);
        this.socket = socket;
    }

    public UserStatus isStatus() {
        return status;
    }

    public void setUser(String nickName, String account) {
        this.nickName = nickName;
        this.account = account;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}