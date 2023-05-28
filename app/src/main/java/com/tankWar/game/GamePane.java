package com.tankWar.game;

import com.tankWar.game.entity.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class GamePane extends BorderPane {
    // 用于绘制的组件
    Canvas canvas = new Canvas();
    GraphicsContext context = canvas.getGraphicsContext2D();

    // 游戏元素
    List<Tank> tanks = new ArrayList<>();
    Tank myTank; // 我的坦克
    Tank testTank; // 测试坦克
    List<Bullet> bullets = new ArrayList<>(); // 子弹列表
    List<Building> buildings = new ArrayList<>(); // 建筑方块列表

    // 游戏逻辑控制参数
    boolean keyJPressed = false; // 是否按下发射键
    boolean hasFired = true; // 是否已处理开火
    LinkedHashSet<KeyCode> keyCodes = new LinkedHashSet<KeyCode>(); // 存储按下的方向键

    // 构造函数
    GamePane() {
        this.init();
    }

    // GamePane初始化函数
    void init() {
        // 载入地图
        loadMap("/map/map.txt");

        // 设置GamePane
        this.setWidth(Config.MapWidth);
        this.setHeight(Config.MapHeight);
        this.setStyle("-fx-background-color: Black");
        this.setPadding(new Insets(Config.MapPaddingSize));
        this.setCenter(canvas);

        // 设置Canvas
        this.canvas.requestFocus();
        this.canvas.setFocusTraversable(true);
        canvas.setWidth(Config.MapWidth);
        canvas.setHeight(Config.MapHeight);

        // 初始化玩家坦克
        myTank = new Tank(Config.MapWidth / 2, Config.MapHeight / 2 - 15, 1);
        testTank = new Tank(Config.MapWidth / 2, Config.MapHeight - 50, 2);
        /*
        初始化在线玩家坦克
        ...
        */
        tanks.add(myTank);
        tanks.add(testTank);

        // 创建显示游戏的线程
        Thread showThread = new Thread(showTask);
        showThread.start();

        // 创建处理元素移动和碰撞的线程
        Thread logicThread = new Thread(logicTask);
        logicThread.start();

        // 按下事件监听
        this.setOnKeyPressed(e -> {
            // 将按下的按键加入按键集合
            KeyCode code = e.getCode();
            // 若为方向键，则假如方向处理列表
            if ((code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT)) {
                keyCodes.add(code);
            }
            // 若为发射键，则修改状态为J键已按下未处理开火
            else if (code == KeyCode.J && !keyJPressed) {
                hasFired = false;
                keyJPressed = true;
            }
        });
        // 松开事件监听: 将松开的按键从集合中删除
        this.setOnKeyReleased(e -> {
            keyCodes.remove(e.getCode());
            if (e.getCode() == KeyCode.J && keyJPressed) {
                keyJPressed = false;
            }
        });
    }

    // 加载地图函数
    void loadMap(String MapFilePath) {
        int row = 0;
        int column = 0;
        try {
            String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources";
            File file = new File(projectPath, MapFilePath);
            Scanner scanner = new Scanner(file);
            row = 0;
            // 逐行读取地图文件内容
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // 处理每行的字符
                int i;
                for (i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    // 根据字符映射到地图元素
                    buildings.add(new Building(i * Config.BlockSize + Config.BlockSize / 2, row * Config.BlockSize + Config.BlockSize / 2, c));
                }
                if (column < i) column = i;
                row++;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 设置地图大小
        Config.BlockXNumber = column;
        Config.BlockYNumber = row;
        Config.MapWidth = Config.BlockXNumber * Config.BlockSize;
        Config.MapHeight = Config.BlockYNumber * Config.BlockSize;
    }

    // 显示游戏界面Task
    Task<Void> showTask = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (true) {
                // 延时
                Thread.sleep(1000 / 60);
                // 使用runLater来多线程处理JavaFX组件
                Platform.runLater(() -> showGame());
            }
        }
    };

    // 显示游戏画面
    private void showGame() {
        // 每次显示都擦除整个界面，防止残影
        this.context.clearRect(0, 0, Config.MapWidth, Config.MapHeight);
        // 绘制坦克
        for (int i = tanks.size() - 1; i >= 0; i--) {
            Tank tank = tanks.get(i);
            tank.draw(context);
        }
        // 绘制子弹
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.move();
            bullet.draw(context);
            // 若子弹已死亡，则移除列表
            if (!bullet.isAlive()) {
                bullets.remove(i);
            }
        }
        // 绘制建筑方块
        for (int i = buildings.size() - 1; i >= 0; i--) {
            Building building = buildings.get(i);
            building.draw(context);
            // 若建筑方块已死亡，则移除列表
            if (!building.isAlive()) {
                buildings.remove(i);
            }
        }
    }

    // 游戏逻辑处理Task
    Task<Void> logicTask = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (true) {
                // 延时
                Thread.sleep(1000 / 60);

                // 处理本机坦克开火
                if (!hasFired) {
                    bullets.add(myTank.fire());
                    bullets.add(testTank.fire());
                    hasFired = true;
                }

                // 处理移动按键输入
                for (KeyCode code : keyCodes) {
                    switch (code) {
                        case UP:
                            myTank.move(Direction.UP);
                            break;
                        case DOWN:
                            myTank.move(Direction.DOWN);
                            break;
                        case LEFT:
                            myTank.move(Direction.LEFT);
                            break;
                        case RIGHT:
                            myTank.move(Direction.RIGHT);
                            break;
                        default:
                            System.out.println("Input error");
                    }
                    break;
                }

                // 处理碰撞
                processCollide();

            }
        }
    };

    // 处理碰撞函数
    void processCollide() {
        // 处理子弹与建筑方块的碰撞
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = buildings.size() - 1; j >= 0; j--) {
                Building building = buildings.get(j);
                // 若方块可穿过以及与子弹碰撞
                if (!building.canGoThough() && bullet.isCollidingWith(building)) {
                    bullet.setAlive(false); // 子弹设置死亡
                    // 若方块是可击碎的，则设置方块死亡
                    if (building.isFragile()) {
                        building.setAlive(false);
                    }
                }
            }
        }
        // 处理坦克与子弹/建筑方块的碰撞
        for (int i = tanks.size() - 1; i >= 0; i--) {
            Tank tank = tanks.get(i);
            boolean isCollide = false;
            if (tank.isAlive()) {
                // 处理坦克与子弹的碰撞
                for (int j = bullets.size() - 1; j >= 0; j--) {
                    Bullet bullet = bullets.get(j);
                    if (bullet.id != tank.id && tank.isCollidingWith(bullet)) {
                        bullet.setAlive(false);
                    }
                }
                // 处理坦克与建筑方块的碰撞
                for (int j = buildings.size() - 1; j >= 0; j--) {
                    Building building = buildings.get(j);
                    if (!building.canGoThough() && tank.isCollidingWith(building)) {
                        isCollide = true;
                    }
                }
                // 处理坦克与坦克的碰撞
                for (int k = tanks.size() - 1; k >= 0; k--) {
                    if (k == i) continue;
                    Tank tankCollide = tanks.get(k);
                    if (tank.isCollidingWith(tankCollide)) {
                        isCollide = true;
                    }
                }
                // 若坦克有碰撞，则设置坦克不能移动
                if (isCollide) {
                    tank.canMove = false;
                } else tank.canMove = true;
            }
        }
    }
}
