package com.tankWar.game.entity;

import com.tankWar.game.Config;

import java.io.InputStream;
import java.util.Scanner;
import java.util.Vector;

public class GameMap {
    int id = -1;
    int row, col;
    Vector<Building> buildings = new Vector<>();
    Tank[] tanks;

    // 用于判断地图是否加载成功
    boolean flag = false;

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

        try (Scanner scanner = new Scanner(inputStream)) {
            // 1. 解析玩家信息
            int playerNum = scanner.nextInt();
            System.out.println(playerNum);
            tanks = new Tank[playerNum];
            for (int i = 0; i < playerNum; i++) {
                int x = scanner.nextInt(), y = scanner.nextInt();
                Direction dir = Direction.valueOf(scanner.next().trim());
//                System.out.println(x + " " + y + " " + dir.toString());

                tanks[i] = new Tank(i, x, y, dir);
            }

            // 2. 解析地图信息
            this.row = scanner.nextInt();
            this.col = scanner.nextInt();

            // 记录建筑方块信息
            scanner.nextLine(); // 这里用于达到下一行
            for (int i = 0; i < row && scanner.hasNextLine(); i++) {
                // 处理每行的字符
                String line = scanner.nextLine().trim();
                for (int j = 0; j < col && j < line.length(); j++) {
                    char c = line.charAt(j);
                    if (c != ' ') {
                        // 根据字符映射到地图元素
                        buildings.add(new Building(j * Config.BlockSize + Config.BlockSize / 2, i * Config.BlockSize + Config.BlockSize / 2, c));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.flag = false;
            return false;
        }

        this.flag = true;
        return true;
    }

    String getPath(int id) {
        return "/map/" + id + ".map";
    }

    public Vector<Building> getBuildings() {
        // 复制一份建筑信息
        return flag ? (Vector<Building>) buildings.clone() : null;
    }

    public Tank[] getTanks() {
        if(!flag) {
            return null;
        }

        // 使用这种方法 复制坦克信息
        int size = this.tanks.length;
        Tank[] copyTanks = new Tank[size];
        for(int i=0; i<size; i++) {
            copyTanks[i] = new Tank(tanks[i]);
        }

        return copyTanks;
    }

    public float getId() {
        return id;
    }
}
