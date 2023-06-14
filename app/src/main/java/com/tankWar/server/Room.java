package com.tankWar.server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;

public class Room {
    // Room信息 (房号使用房主账号标记)
    String roomNum, roomName, password;
    int maxUserNum;
    boolean havePassword;

    // Room状态
    boolean status = false;
    Game game = null;

    // 所有用户
    HashMap<SocketChannel, User> users = new HashMap<>();


    public Room(String roomNum, String roomName, int userNum)  {
        this.roomNum = roomNum;
        this.roomName = roomName;
        this.maxUserNum = userNum;
        this.havePassword = false;
    }

    public Room(String roomNum, String roomName, int userNum, String password){
        this(roomNum, roomName, userNum);
        this.havePassword = true;
        this.password = password;
    }

    //清空房间内的所有内容
    public void cleanAll(){
        // 修改玩家状态
        for(User user: users.values()) {
            user.setStatus(UserStatus.Null);
            user.leaveRoom();
        }

        // 清除房间的所用用户
        users.clear();
    }

    // 添加房间里的玩家昵称、账号、套接字和状态。初始状态是wait
    public void addOnlineUser(SocketChannel socket, User user){
        user.setStatus(UserStatus.NoReady);
        user.joinRoom(this);
        // 第一个玩家就是房主
        users.put(socket, user);
    }

    //通过索引移除房间里的玩家
    public void removeOnlineUser(SocketChannel socket){
        // 如果玩家不存在则直接返回
        if(!users.containsKey(socket)) {
            return;
        }
        User user = users.get(socket);
        user.setStatus(UserStatus.Null);
        user.leaveRoom();
        // 删除玩家
        users.remove(socket);
    }

    //返回进入房间的人数
    public int getOnlineUserNum() {
        return users.size();
    }

    public HashMap<SocketChannel, User> getAllUsers() {
        return users;
    }

    // 根据用户名查找用户
    public Pair<SocketChannel, User> getUser(String nickName) {
        for(Map.Entry<SocketChannel, User> e: users.entrySet()) {
            if(e.getValue().getNickName().equals(nickName)) {
                return new Pair<>(e.getKey(), e.getValue());
            }
        }

        return null;
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

    //返回房间里设置的人数
    public int getMaxUserNum() {
        return maxUserNum;
    }

    //返回房间是否设置了密码
    public boolean havePassword() {return havePassword;}
    //返回房间的密码
    public String getPassword(){return password;}
    // 设置密码
    public void setPassword(String password) {
        this.password = password;
    }

    //返回房间的状态
    public boolean getStatus() {
        return status;
    }

    // 返回是否满了
    public boolean isFull() {
        return users.size() >= maxUserNum;
    }

    public boolean isHost(String name) {
        return users.get(0).getNickName().equals(name);
    }

    public boolean checkPassword(String password) {
        return password.equals(this.password);
    }

    // 检查是否房间中的用户都是准备好的
    public boolean checkAllUsersReady() {
        boolean flag = true;
        for (User user: users.values())
            flag &= user.getStatus() == UserStatus.Ready;
        return flag;
    }

    // 改变房间的状态 和 玩家状态
    public void startGame(boolean status){
        status = true;
        // 设置所有玩家为游戏状态
        for(User user: users.values())
            user.setStatus(UserStatus.Playing);

        // todo 初始化操作
        game = new Game(this.users);
    }

    public Game getGame() {
        return game;
    }
}
