package com.tankWar.server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.tankWar.utils.Pair;

public class Room {
    // Room信息 (房号使用房主账号标记)
    String roomNum, roomName, password;
    int maxUserNum;
    boolean havePassword;

    // Room的状态
    boolean status = false;
    Game game = null;

    // 所有用户
    Vector<SocketChannel> sockets;
    HashMap<SocketChannel, User> users = new HashMap<>();


    public Room(String roomNum, String roomName, int userNum)  {
        this.roomNum = roomNum;
        this.roomName = roomName;
        this.maxUserNum = userNum;
        this.havePassword = false;

        sockets = new Vector<>(userNum);
    }

    public Room(String roomNum, String roomName, int userNum, String password){
        this(roomNum, roomName, userNum);
        this.havePassword = true;
        this.password = password;

        sockets = new Vector<>(userNum);
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
        sockets.add(socket);
        users.put(socket, user);
    }

    //通过索引移除房间里的玩家
    public void removeOnlineUser(SocketChannel socket){
        if(!users.containsKey(socket))
            return;

        users.get(socket).setStatus(UserStatus.Null);
        sockets.remove(socket);
        users.remove(socket);
    }

    //返回进入房间的人数
    public int getOnlineUserNum() {
        return users.size();
    }

    // 获得房间内的所有Socket
    public Vector<SocketChannel> getAllSockets() {
        return sockets;
    }

    public Vector<User> getAllUsers() {
        Vector<User> retUsers = new Vector<>(sockets.size());
        for(SocketChannel socket: sockets)
            retUsers.add(users.get(socket));
        return retUsers;
    }

    // 根据用户名查找用户
    public Pair<SocketChannel, User>  getUser(String nickName) {
        for(Map.Entry<SocketChannel, User> pair: users.entrySet()) {
            User user = pair.getValue();
            if(user.getNickName().equals(nickName)) {
                return new Pair<>(pair.getKey(), pair.getValue());
            }
        }

        return null;
    }

    // 获得房间内所有玩家的用户名
    public String[] getAllNickNames() {
        String[] names = new String[users.size()];
        int i = 0;
        for(SocketChannel socket: sockets)
            names[i++] = users.get(socket).getNickName();
        return names;
    }

    //返回该房间的房间号，我是直接用房主的账号做房间号的
    public String getRoomNum(){return roomNum;}

    //返回房间名称
    public String getRoomName(){
        return roomName;
    }

    // 返回房主名 (根据房间号查找 房间号==房主账号)
    public String getHostName(){
        return users.get(sockets.get(0)).getNickName();
    }

    //返回房间里设置的人数
    public int getMaxUserNum() {
        return maxUserNum;
    }

    //返回房间的状态
    public boolean isPlaying() {
        return status;
    }

    // 返回是否满了
    public boolean isFull() {
        return users.size() >= maxUserNum;
    }

    // 根据用户账号判断是否是房主
    public boolean isHost(String account) {
        return roomNum.equals(account);
    }

    //返回房间是否设置了密码
    public boolean havePassword() {return havePassword;}

    // 设置密码
    public void setPassword(String password) {
        this.password = password;
    }

    // 验证密码
    public boolean checkPassword(String password) {
        return password.equals(this.password);
    }

    // 检查是否房间中的用户都是准备好的
    public boolean checkAllUsersReady() {
        boolean flag = true;
        for(User user: users.values())
            flag &= user.getStatus() == UserStatus.Ready;
        return flag;
    }


    // 改变房间的状态 和 玩家状态
    public void startGame(){
        this.status = true;

        // 设置所有玩家为游戏状态
        for(User user: users.values())
            user.setStatus(UserStatus.Playing);

        // 初始化操作
        game = new Game(sockets);
    }

    // 改变房间的状态 和 玩家状态
    public void endGame(){
        this.status = false;
        // 设置所有玩家为游戏状态
        for(User user: users.values())
            user.setStatus(UserStatus.NoReady);
//        System.out.println("change user status!!!!!!!!!!!");
    }

    public Game getGame() {
        return game;
    }
}
