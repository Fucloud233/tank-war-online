package com.tankWar.game;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Vector;

public class GamePane extends BorderPane {
    // 用于绘制的组件
    Canvas canvas = new Canvas();
    GraphicsContext context = canvas.getGraphicsContext2D();

    // 设置地图大小（格数）
    int nWidth = 32, nHeight = 32;

    // 游戏元素
    Tank[] tanks;
    Vector<Bullet> bullets;

    GamePane() {
        this.init();
        this.drawMap();
    }

    void init() {
        int padding = 32;
        int w = nWidth*Block.getSize();
        int h = nHeight*Block.getSize();

        this.setWidth(w);
        this.setMinWidth(w);
        this.setHeight(h);
        this.setMinHeight(h);
        this.setStyle("-fx-background-color: Black");
        this.setPadding(new Insets(padding));
        this.setCenter(canvas);

        canvas.setWidth(this.getWidth());
        canvas.setHeight(this.getHeight());
    }

    void drawMap() {
        // 设置背景
        context.setFill(Color.BLACK);
        context.fillRect(0, 0, this.getWidth(), this.getHeight());

        // 绘制网格
        int size = Block.size;
        context.setStroke(Color.WHITE);
        for(int i=0; i<nWidth+1; i++)
            context.strokeLine(i*size, 0,  i*size, nHeight*size);

        for(int i=0; i<nWidth+1; i++)
            context.strokeLine(0,  i*size, nHeight*size, i*size);
    }
}
