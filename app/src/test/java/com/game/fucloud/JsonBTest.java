package com.game.fucloud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tankWar.game.entity.Tank;
import org.junit.Test;

public class JsonBTest {

    @Test
    public void jsonbTest() throws JsonProcessingException {
        String tankStr = "{\"x\":200.0,\"y\":200.0,\"id\":1}";
        System.out.println("[info] 坦克方向:"+tankStr);

        ObjectMapper mapper = new ObjectMapper();

        Tank tank = mapper.readValue(tankStr, Tank.class);

        System.out.println("[info] 坦克坐标:"+tank.getX() + tank.getY() + tank.getId());

    }
}
