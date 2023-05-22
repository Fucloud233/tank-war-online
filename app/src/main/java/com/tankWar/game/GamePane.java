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

import java.util.LinkedHashSet;
import java.util.Vector;

public class GamePane extends BorderPane {
    // 用于绘制的组件
    Canvas canvas = new Canvas();
    GraphicsContext context = canvas.getGraphicsContext2D();

    // 游戏元素
    Tank[] tanks;
    Tank myTank;
    Vector<Bullet> bullets;
    Vector<Building> buildings;

    GamePane() {
        this.init();
    }

    LinkedHashSet<KeyCode> keyCodes = new LinkedHashSet<KeyCode>();


    // JavaFX中使用Task来处理擦长时间多线程任务（还在调研中）
    Task<Void> task = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while(true) {
                // 延时
                Thread.sleep(1000/60);

                for(KeyCode code: keyCodes) {
                    switch(code) {
                        case UP: myTank.move(Direction.UP, Config.TankSpeed); break;
                        case DOWN: myTank.move(Direction.DOWN, Config.TankSpeed); break;
                        case LEFT: myTank.move(Direction.LEFT, Config.TankSpeed); break;
                        case RIGHT: myTank.move(Direction.RIGHT, Config.TankSpeed); break;
                        default: System.out.println("Input error");
                    }
                    break;
                }

                // 使用runLater来多线程处理JavaFX组件
                Platform.runLater(()->showGame());
            }
        }
    };

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

        myTank = new Tank(Config.MapWidth/2, Config.MapHeight/2, Direction.DOWN);

        // 创建处理坦克移动的线程
        Thread thread = new Thread(task);
        thread.start();

        // 按下事件监听
        this.setOnKeyPressed(e->{
            // 将按下的按键加入按键集合
            KeyCode code = e.getCode();
            if((code==KeyCode.UP || code==KeyCode.DOWN || code==KeyCode.LEFT || code==KeyCode.RIGHT)){
                keyCodes.add(code);
            }
        });

        // 松开事件监听: 将松开的按键从集合中删除
        this.setOnKeyReleased(e-> {
            keyCodes.remove(e.getCode());
        });

        showGame();
    }

    // 显示游戏画面
    private void showGame() {
        this.context.clearRect(0, 0, Config.MapWidth, Config.MapHeight);
        drawBackground();
        this.myTank.draw(context);
    }

    // 绘制背景
    void drawBackground() {
        // 设置背景
        context.setFill(Color.BLACK);
        context.fillRect(0, 0, Config.MapWidth, Config.MapHeight);

        // 绘制网格
        int size = Config.BlockSize;
        context.setStroke(Color.WHITE);
        for(int i=0; i<Config.BlockXNumber+1; i++)
            context.strokeLine(i*size, 0,  i*size, Config.MapHeight);

        for(int i=0; i<Config.BlockYNumber+1; i++)
            context.strokeLine(0,  i*size, Config.MapWidth, i*size);
    }
}
