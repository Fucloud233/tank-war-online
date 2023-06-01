package com.tankWar.lobby;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

public class Room {
    private String RoomNum;
    private boolean is_used=false;
    private int user_num;
    private int enter_num=0;
    private String password;
    private ArrayList<String> players = new ArrayList<>();
    /////////////////////////
    private static Vector onlineUser = new Vector(10, 5);//保存在线用户的用户名
    private static Vector socketUser = new Vector(10, 5);//保存在线用户的Socket对象
    ///////////////////////////

    public Room(String roomNum,int userNum,String passw){
        RoomNum=roomNum;
        user_num=userNum;
        password=passw;

    }
    public void setIs_used(boolean a){
        is_used=a;
    }
    public void setUser_num(int a){
        user_num=a;

    }
    public void setPassword(String a){
        password=a;

    }
    public void addPlayer(String a){
        players.add(a);
        enter_num++;

    }
    ////////////////////////////////////
    public void addOnlineUser(String name){
        onlineUser.addElement(name);
    }
    public void addSocketUser(Socket s){
        socketUser.addElement(s);
    }
    public void removeOnlineUser(int i){
        onlineUser.removeElementAt(i);
    }
    public void removeSocketUser(int i){
        socketUser.removeElementAt(i);
    }
    public String findOnlineUser(int i){
        return (String) onlineUser.elementAt(i);
    }
    public Socket findSocketUser(int i){
        enter_num-=1;
        return (Socket) socketUser.elementAt(i);
    }
    public int sizeOfOnlineUser(){
        return onlineUser.size();
    }
    /////////////////////////////////////
    public ArrayList<String> getPlayers(){
        return players;
    }

    public String getRoomNum(){return RoomNum;}

    public int getEnter_num() {
        return enter_num;
    }

    public int getUser_num() {
        return user_num;
    }
    public String getPassword(){return password;}

    public boolean isIs_used() {
        return is_used;
    }
    public boolean compareWithPassword(String a){
        if (Objects.equals(password, a)){
            return true;
        }
        else {
            return false;
        }
    }
    ////////////////////////////////
    public boolean compareUserNum(){
        if (enter_num<user_num){
            return true;
        }
        return false;
    }
    public boolean findUserId(String userId) {
        int i = players.indexOf(userId);
        if (i == -1) {
            return false;
        }
        return true;
    }
    /////////////////////////////////
}