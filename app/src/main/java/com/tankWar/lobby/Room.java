package com.tankWar.lobby;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class Room {
    private String RoomNum;//房间号，用房主的账号标识
    private String hostName;//房主名字
    private String RoomName;//房间名字
    private int user_num;//房间的人数上限
    private int enter_num=0;//房间的当前人数
    private boolean is_password;//判断有无密码
    private String password;
    private String roomStatus="未开始";//房间状态
    private static Vector statusUser = new Vector(10,5);//保存房间里用户的状态
    private static Vector nameUser = new Vector(10,5);//保存房间里面的用户昵称
    private static Vector onlineUser = new Vector(10, 5);//保存在线用户的用户名
    private static Vector socketUser = new Vector(10, 5);//保存在线用户的Socket对象

    public Room(boolean isPassword,String userName,String account,String roomName,String userNum,String passWord,Socket s){
        this.is_password=isPassword;
        this.hostName=userName;
        this.RoomNum=account;
        this.RoomName=roomName;
        this.user_num= Integer.parseInt(userNum);
        this.password=passWord;
    }

    //清空房间内的所有内容
    public void ClearALL(){
        statusUser.clear();
        nameUser.clear();
        onlineUser.clear();
        statusUser.clear();
        enter_num=0;
    }

    //添加房间里的玩家昵称、账号、套接字和状态。初始状态是wait
    public void addOnlineUser(String name,String account,Socket s){
        enter_num+=1;

        onlineUser.addElement(account);
        socketUser.addElement(s);
        if(account.equals(RoomNum)){
            //房主必然是准备好的 并且有房主标识
            nameUser.addElement(name+"(房主)");
            statusUser.addElement("已准备");
        }
        else{
            nameUser.addElement(name);
            statusUser.addElement("未准备");
        }

    }
    /////////////////////////检查是否房间中的用户都是准备好的///////////////////////////////////
    public boolean areAllUsersReady() {
        for (int i = 0; i < enter_num; i++) {
            String status = (String) statusUser.elementAt(i);
            if (status.equals("未准备")) {
                return false;
            }
        }
        return true;
    }

    //通过索引移除房间里的玩家
    public void removeOnlineUser(int i){
        enter_num-=1;
        onlineUser.removeElementAt(i);
        socketUser.removeElementAt(i);
        nameUser.removeElementAt(i);
        statusUser.removeElementAt(i);
    }
    //根据账号找到玩家的索引
    public int getAccountIndex(String account){
        return onlineUser.indexOf(account);
    }
    //根据下标找到玩家昵称
    public String findNameUser(int i){
        return (String) nameUser.elementAt(i);
    }
    //根据下标找到玩家账号
    public String findOnlineUser(int i){
        return (String) onlineUser.elementAt(i);
    }

    //根据下标找到玩家套接字
    public Socket findSocketUser(int i){
        return (Socket) socketUser.elementAt(i);
    }


    //根据下标找到玩家状态
    public String findStatusUser(int i){
        System.out.println(statusUser.elementAt(i));
        return (String) statusUser.elementAt(i);
    }
    //切换对应下表的玩家的状态
    public void changeStatusUser(int index) {
        if(findStatusUser(index).equals("未准备")){
            statusUser.setElementAt("已准备", index);
        }
        else{
            statusUser.setElementAt("未准备", index);
        }
        System.out.println("修改后"+statusUser.elementAt(index));
    }




    //返回该房间的房间号，我是直接用房主的账号做房间号的
    public String getRoomNum(){return RoomNum;}
    //返回房间名称
    public String getRoomName(){
        return RoomName;
    }
    //返回房主名
    public String getHostName(){
        return hostName;
    }
    //返回进入房间的人数
    public int getEnter_num() {
        return enter_num;
    }
    //返回房间里设置的人数
    public int getUser_num() {
        return user_num;
    }
    //返回房间是否设置了密码
    public boolean getIs_password(){return is_password;}
    //返回房间的密码
    public String getPassword(){return password;}
    //返回房间的状态
    public String getRoomStatus(){return roomStatus;}
    //改变房间的状态
    public void setRoomStatus(){
        roomStatus="游戏中";
    }


}