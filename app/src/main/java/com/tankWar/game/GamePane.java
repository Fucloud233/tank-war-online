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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

public class GamePane extends BorderPane {
    // 用于绘制的组件
    Canvas canvas = new Canvas();
    GraphicsContext context = canvas.getGraphicsContext2D();

    // 游戏元素
    List<Tank> tanks = new ArrayList<>();
    Tank myTank;
    Tank testTank;
    List<Bullet> bullets = new ArrayList<>();
    List<Building> buildings = new ArrayList<>();

    // 游戏逻辑控制参数
    boolean keyJPressed = false;
    boolean hasFired = true;
    LinkedHashSet<KeyCode> keyCodes = new LinkedHashSet<KeyCode>();

    GamePane() {
        this.init();
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
        for (int i = tanks.size() - 1; i >= 0; i--) {
            Tank tank = tanks.get(i);
            boolean isCollide = false;
            // 处理碰撞
            if (tank.isAlive()) {
                for (int j = bullets.size() - 1; j >= 0; j--) {
                    Bullet bullet = bullets.get(j);
                    if (bullet.id != tank.id && tank.collideWith(bullet)) {
                        bullet.setAlive(false);
                    }
                }
                for (int k = tanks.size() - 1; k >= 0; k--) {
                    if (k == i) continue;
                    Tank tankCollide = tanks.get(k);
                    if (tank.collideWith(tankCollide)) {
                        isCollide = true;
                    }
                }
                if (isCollide) {
                    tank.canMove = false;
                } else tank.canMove = true;
            }
        }

    }

    // GamePane初始化函数
    void init() {
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

        // 初始化坦克
        myTank = new Tank(Config.MapWidth / 2, Config.MapHeight / 2, 1);
        testTank = new Tank(Config.MapWidth / 2, Config.MapHeight - 50, 2);
        tanks.add(myTank);
        tanks.add(testTank);

        // 创建处理元素移动的线程
        Thread showThread = new Thread(showTask);
        showThread.start();

        // 创建处理元素移动的线程
        Thread logicThread = new Thread(logicTask);
        logicThread.start();

        // 按下事件监听
        this.setOnKeyPressed(e -> {
            // 将按下的按键加入按键集合
            KeyCode code = e.getCode();
            if ((code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT)) {
                keyCodes.add(code);
            } else if (code == KeyCode.J && !keyJPressed) {
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

        showGame();
    }

    // 显示游戏画面
    private void showGame() {
        this.context.clearRect(0, 0, Config.MapWidth, Config.MapHeight);
        drawBackground();
        for (int i = tanks.size() - 1; i >= 0; i--) {
            Tank tank = tanks.get(i);
            tank.draw(context);
        }
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.move();
            bullet.draw(context);
            if (!bullet.isAlive()) {
                bullets.remove(i);
            }
        }
    }

    // 绘制背景
    void drawBackground() {
        // 设置背景
        context.setFill(Color.BLACK);
        context.fillRect(0, 0, Config.MapWidth, Config.MapHeight);

        // 绘制网格
        int size = Config.BlockSize;
        context.setStroke(Color.WHITE);
        for (int i = 0; i < Config.BlockXNumber + 1; i++)
            context.strokeLine(i * size, 0, i * size, Config.MapHeight);

        for (int i = 0; i < Config.BlockYNumber + 1; i++)
            context.strokeLine(0, i * size, Config.MapWidth, i * size);
    }
}
