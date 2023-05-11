package com.tankWar.game;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Vector;

public class GamePane extends Pane {

    Canvas canvas = new Canvas();

    GamePane() {
        this.setWidth(400);
        this.setMinWidth(400);
        this.setHeight(300);
        this.setMinHeight(300);

//        this.setPadding(new Insets(10));
        canvas.setWidth(this.getWidth());
        canvas.setHeight(this.getHeight());


        canvas.getGraphicsContext2D().strokeText("hello", 150, 100);

//        this.drawMap();
        this.getChildren().add(canvas);

    }

    int nWidth = 10;
    int nWeight = 10;

//    void drawMap() {
//        context.setFill(Color.BLACK);
//        context.fillRect(0, 0, this.getWidth(), this.getHeight());
//    }
}
