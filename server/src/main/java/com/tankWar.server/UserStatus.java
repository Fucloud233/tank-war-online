package com.tankWar.server;

public enum UserStatus {
    Null, NoReady, Ready, Playing;

//    Null(0), NoReady(1), Ready(2), Playing(3);

//    int value;
//    UserStatus(int value) {
//        this.value = value;
//    }

    public boolean isInLobby() {
        return this == Null;
    }

    public boolean isInRoom() {
        return this == NoReady || this == Ready;
    }

    public boolean isPlaying() {
        return this == Playing;
    }
}
