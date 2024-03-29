package com.tankWar.client.game;

import com.tankWar.entity.Direction;
import javafx.scene.input.KeyCode;

import java.util.HashMap;

public class Utils {
    // 用来转换KeyCode 和 Direction
    public static HashMap<KeyCode, Direction> DirMap = new HashMap<KeyCode, Direction>() ;
    static {
        DirMap.put(KeyCode.LEFT, Direction.LEFT);
        DirMap.put(KeyCode.RIGHT, Direction.RIGHT);
        DirMap.put(KeyCode.UP, Direction.UP);
        DirMap.put(KeyCode.DOWN, Direction.DOWN);
    };


    // 用来判断方向是否有效
    public static boolean CheckCodeIsMove(KeyCode code) {
        return code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT;
    }
}