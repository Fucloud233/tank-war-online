package com.tankWar.server;

import java.net.Socket;

public class User {
    String nickName;
    String account;
    String password;
    UserStatus status = UserStatus.Null;

    Room room = null;

    public User(String nickName, String account, String password) {
        this.nickName = nickName;
        this.account = account;
        this.password = password;
    }

    public User(String nickName, String account) {
        this(nickName, account, null);
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserStatus getStatus() {
        return this.status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public void joinRoom(Room room) {
        this.room = room;
    }

    public void leaveRoom() {
        this.room = null;
    }
}
