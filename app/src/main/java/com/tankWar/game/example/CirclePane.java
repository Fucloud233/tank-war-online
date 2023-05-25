package com.tankWar.game.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.HashSet;
import java.util.Set;

public class CirclePane extends HBox {
    // 窗格大小
    int paneSize = 900;

    // 圈的参数
    int X = 50, Y = 50, size = 40;
    int speed = 5;

    Canvas canvas = new Canvas();
    GraphicsContext context = canvas.getGraphicsContext2D();

    Set<KeyCode> keyCodes = new HashSet<KeyCode>();

    Task<Void> task = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while(true) {
                // 延时
                Thread.sleep(1000/60);

                for(KeyCode code: keyCodes) {
                    switch(code) {
                        case UP: Y -= speed; break;
                        case DOWN: Y += speed; break;
                        case LEFT: X -= speed; break;
                        case RIGHT: X += speed; break;
                        default: System.out.println("Input error");
                    }
                }

                // 使用runLater来多线程处理JavaFX组件
                Platform.runLater(()->drawCircle());
            }
        }
    };

    // 构造函数
    public CirclePane() {
        canvas.setWidth(paneSize);
        canvas.setHeight(paneSize);
        this.getChildren().add(canvas);

        // 线程相关
        Thread thread = new Thread(task);
        thread.start();



        // 按下时间监听
        this.setOnKeyPressed(e->{
            // 将按下的按键加入按键集合
            KeyCode code = e.getCode();
            if(code==KeyCode.UP || code==KeyCode.DOWN || code==KeyCode.LEFT || code==KeyCode.RIGHT)
                keyCodes.add(code);
        });

        // 松开事件监听: 将松开的按键从集合中删除
        this.setOnKeyReleased(e-> keyCodes.remove(e.getCode()));

        // 绘制图像
        drawCircle();
    }

    void drawCircle() {
        // 绘制背景
        context.setFill(Color.BLACK);
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        context.setFill(Color.WHITE);
        context.fillOval(X, Y, size, size);
    }
}