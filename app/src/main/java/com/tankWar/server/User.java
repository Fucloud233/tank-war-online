package com.tankWar.server;

import java.net.Socket;

public class User {
    String nickName;
    String account;
    String password;

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
}
