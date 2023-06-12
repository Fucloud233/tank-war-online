package com.tankWar.server;

import java.net.Socket;
import java.util.Vector;

public class Room {
    // Room信息 (房号使用房主账号标记)
    String roomNum;
    String roomName;
    int maxUserNum;
    String password;
    boolean havePassword;

    // Room状态
    private boolean status = false;

    // 房主信息
    int curUserNum = 0; //房间的当前人数
    Vector<UserData> users;

    public Room(String roomNum, String roomName, int userNum){
        this.roomNum = roomNum;
        this.roomName = roomName;
        this.maxUserNum = userNum;
        this.havePassword = false;

        users = new Vector<>(userNum);
    }

    public Room(String roomNum, String roomName, int userNum, String password){
        this(roomNum, roomName, userNum);
        this.havePassword = true;
        this.password = password;
    }

    //清空房间内的所有内容
    public void cleanAll(){
        curUserNum=0;
    }

    // 添加房间里的玩家昵称、账号、套接字和状态。初始状态是wait
    public void addOnlineUser(User user, Socket s){
        // 第一个玩家就是房主
        users.add(new UserData(user, s, false));
        curUserNum += 1;
    }

    //通过索引移除房间里的玩家
    public void removeOnlineUser(int i){
        users.removeElementAt(i);
        curUserNum -= 1;
    }

    // 根据下标找到玩家状态
    public String getUserStatus(int i){
//        System.out.println("[info] "+statusUser.elementAt(i));
        return users.get(i).isStatus() ? "已准备" : "未准备";
    }


    // 切换对应下表的玩家的状态
    public void changeUserStatus(int index, boolean status) {
        UserData user = users.get(index);
        user.setStatus(!user.isStatus());
//        System.out.println("[info] 修改后"+statusUser.elementAt(index));
    }

    public void changeUserStatus(String name, boolean status) {
        for(UserData user: users) {
            if(user.getNickName().equals(name)) {
                user.setStatus(status);
                break;
            }
        }
//        System.out.println("[info] 修改后"+statusUser.elementAt(index));
    }

    /////////////////////////检查是否房间中的用户都是准备好的///////////////////////////////////
    public boolean areAllUsersReady() {
        boolean flag = true;
        for (UserData user: users)
            flag &= user.isStatus();
        return flag;
    }

    // 根据账号找到玩家的索引
//    public int getAccountIndex(String account){
//        return onlineUser.indexOf(account);
//    }

    //根据下标找到玩家昵称
    public String findNameUser(int i){
        return users.get(i).getNickName();
    }

    //根据下标找到玩家账号
    public String findOnlineUser(int i){
        return users.get(i).getAccount();
    }

    //根据下标找到玩家套接字
    public Socket findSocketUser(int i){
        return users.get(i).getSocket();
    }

    public Socket[] getAllSockets() {
        Socket[] sockets = new Socket[users.size()];
        for(int i=0; i<users.size(); i++)
            sockets[i] = users.get(i).getSocket();
        return sockets;
    }

    public String[] getAllNickNames() {
        String[] names = new String[users.size()];
        for(int i=0; i<users.size(); i++)
            names[i] = users.get(i).getNickName();
        return names;
    }

    //返回该房间的房间号，我是直接用房主的账号做房间号的
    public String getRoomNum(){return roomNum;}

    //返回房间名称
    public String getRoomName(){
        return roomName;
    }

    //返回房主名
    public String getHostName(){
        return users.get(0).getNickName();
    }


    //返回进入房间的人数
    public int getCurUserNum() {
        return users.size();
    }

    //返回房间里设置的人数
    public int getMaxUserNum() {
        return maxUserNum;
    }

    //返回房间是否设置了密码
    public boolean havePassword() {return havePassword;}
    //返回房间的密码
    public String getPassword(){return password;}
    //返回房间的状态
    public boolean getStatus() {
        return status;
    }
    //改变房间的状态
    public void setStatus(boolean status){
        status = true;
    }


    // 返回是否满了
    public boolean isFull() {
        return users.size() >= maxUserNum;
    }

    public boolean checkPassword(String password) {
        return password.equals(this.password);
    }

    public boolean isHost(String name) {
        return users.get(0).getNickName().equals(name);
    }
}

class UserData extends User {
    boolean status;
    Socket socket;

    public UserData(User user, Socket socket, boolean status) {
        super(user.nickName, user.account, user.password);
        this.status = status;
        this.socket = socket;
    }

    public UserData(User user, Socket socket) {
        this(user, socket, false);
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}