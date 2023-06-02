package com.tankWar.game.msg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tankWar.game.entity.Tank;

public class InitMsg extends Message{

    Tank[] tanks = null;

    public InitMsg() {
        super();
    }

    @JsonCreator
    public InitMsg(@JsonProperty("id") int id, @JsonProperty("tanks") Tank[] tanks) {
        super(id, MessageType.Init);

        this.tanks = tanks;
    }

    public Tank[] getTanks() {
        return tanks;
    }

    public void setTanks(Tank[] tanks) {
        this.tanks = tanks;
    }
}
