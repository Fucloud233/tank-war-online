package com.tankWar.lobby;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class Room {
    private String RoomNum;
    private boolean is_used=false;
    private int user_num;
    private int enter_num=0;
    private String password;
    private ArrayList<String> players = new ArrayList<>();
    private static Vector onlineUser = new Vector(10, 5);//保存在线用户的用户名
    private static Vector socketUser = new Vector(10, 5);//保存在线用户的Socket对象

    public Room(String roomNum,int userNum,String passw){
        RoomNum=roomNum;
        user_num=userNum;
        password=passw;

    }
    //设置房间的使用状态
    public void setIs_used(boolean a){
        is_used=a;
    }
    //设置房间的人数
    public void setUser_num(int a){
        user_num=a;

    }
    //设置房间的密码
    public void setPassword(String a){
        password=a;

    }
    //添加房间里的玩家账号，好像和下面的函数重复了，你可以改改，sorry~
    public void addPlayer(String a){
        players.add(a);
        enter_num++;

    }
    //添加房间里的玩家账号
    public void addOnlineUser(String name){
        onlineUser.addElement(name);
    }
    //添加房间里的玩家套接字，注意因为这个函数和上面那个函数往往都是同步添加的，所以它们的下标一样的话，对应的是一个用户
    //可以再添加的时候合在一起加
    public void addSocketUser(Socket s){
        socketUser.addElement(s);
    }
    //移除房间里的玩家账号
    public void removeOnlineUser(int i){
        enter_num-=1;
        onlineUser.removeElementAt(i);
    }
    //移除房间里的玩家套接字，感觉这个可以和上面的函数合在一起操作
    public void removeSocketUser(int i){
        socketUser.removeElementAt(i);
    }
    //根据下标找到玩家账号
    public String findOnlineUser(int i){
        return (String) onlineUser.elementAt(i);
    }
    //根据下标找到玩家套接字
    public Socket findSocketUser(int i){

        return (Socket) socketUser.elementAt(i);
    }
    //返回房间人数，好像和getEnter_num重复了
    public int sizeOfOnlineUser(){
        return onlineUser.size();
    }
    //返回该房间的房间号，我是直接用房主的账号做房间号的
    public String getRoomNum(){return RoomNum;}
    //返回进入房间的人数
    public int getEnter_num() {
        return enter_num;
    }
    //返回房间里设置的人数
    public int getUser_num() {
        return user_num;
    }
    //返回房间的密码
    public String getPassword(){return password;}
    //返回房间是否被使用的状态
    public boolean isIs_used() {
        return is_used;
    }
    //查看房间内是否已经有用户的账号，有的话就不重复添加了
    public boolean findUserId(String userId) {
        int i = players.indexOf(userId);
        if (i == -1) {
            return false;
        }
        return true;
    }

}