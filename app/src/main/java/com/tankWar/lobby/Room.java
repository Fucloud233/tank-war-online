package com.tankWar.lobby;

import java.util.ArrayList;
import java.util.Objects;

public class Room {
    private int RoomNum;
    private boolean is_used=false;
    private int user_num;
    private int enter_num=0;
    private String password;
    private ArrayList<String> players = new ArrayList<>();

    public Room(int roomNum){
        RoomNum=roomNum;

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
    public ArrayList<String> getPlayers(){
        return players;
    }

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
    public boolean comparewithpassw(String a){
        if (Objects.equals(password, a)){
            return true;
        }
        else {
            return false;
        }
    }
}