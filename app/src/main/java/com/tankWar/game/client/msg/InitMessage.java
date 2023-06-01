package com.tankWar.game.client.msg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tankWar.game.entity.Tank;

public class InitMessage extends Message{

    Tank[] tanks = null;

    public InitMessage() {
        super();
    }

    @JsonCreator
    public InitMessage(@JsonProperty("id") int id, @JsonProperty("tanks") Tank[] tanks) {
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
