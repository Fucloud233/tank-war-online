package com.tankWar.game.entity;

import com.tankWar.game.Config;

import java.io.InputStream;
import java.util.Scanner;
import java.util.Vector;

public class GameMap {
    int id = -1;
    int row, col;
    Vector<Building> buildings = new Vector<>();

    public GameMap() {
    }

    public boolean loadMap(int id) {
        // 设置地图编号
        this.id = id;

        // 打开地图文件
        String path = this.getPath(id);
        InputStream inputStream = getClass().getResourceAsStream(path);
        if(inputStream == null) {
            System.out.println("[error]"+path + "不存在!");
            return false;
        }
        Scanner scanner = new Scanner(inputStream);

        // 记录建筑方块信息
        // 逐行读取地图文件内容
        this.row = this.col = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // 处理每行的字符
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c != ' ') {
                    // 根据字符映射到地图元素
                    buildings.add(new Building(i * Config.BlockSize + Config.BlockSize / 2, row * Config.BlockSize + Config.BlockSize / 2, c));
                }
            }

            if (col < line.length())
                col = line.length();
            row++;
        }
        return true;
    }

    String getPath(int id) {
        return "/map/" + id + ".map";
    }

    public Vector<Building> getBuildings() {
        // 复制一份建筑信息
        return (Vector<Building>) buildings.clone();
    }

    public float getId() {
        return id;
    }

    public float getMapWidth() {
        return Config.BlockSize * row ;
    }

    public float getMapHeight() {
        return Config.BlockSize * col;
    }
}
