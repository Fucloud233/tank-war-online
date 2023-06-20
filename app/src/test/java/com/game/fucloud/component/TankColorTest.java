package com.game.fucloud.component;

import com.tankWar.App;
import com.tankWar.game.Config;
import com.tankWar.game.entity.Direction;
import com.tankWar.game.entity.Tank;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.Test;


public class TankColorTest extends Application {

    @Test
    public void test() {
        Color color = Color.RED;
        System.out.println("hello");
        System.out.println(color.getRed());
        System.out.println(color.getGreen());
        System.out.println(color.getBlue());
//        System.out.printf("RED %f %f %f %f", color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());

        Application.launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas();
        canvas.setWidth(200);
        canvas.setHeight(200);
        GraphicsContext context = canvas.getGraphicsContext2D();

        Tank tank = new Tank(0, 0, 0, Direction.LEFT);
        Image image = new Image("image/up.png");
//        Image image = new Image("image/UP.png");
        image = modifyColor(image);

        context.setStroke(Color.BLACK)  ;
        context.fillRect(0, 0, 200, 200);
        context.drawImage(image, 100, 100);

        HBox mainPane = new HBox();
        mainPane.getChildren().add(canvas);
        Scene scene = new Scene(mainPane);

        stage.setScene(scene);
        stage.show();

    }

    public Image modifyColor(Image sourceImage) {
        final int w = (int) sourceImage.getWidth();
        final int h = (int) sourceImage.getHeight();
        final WritableImage outputImage = new WritableImage(w, h);
        final PixelWriter writer = outputImage.getPixelWriter();
        final PixelReader reader = sourceImage.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // 使用颜色叠加法
                Color color = Color.RED;
                Color newColor = colorPlus(reader.getColor(x, y), colorMultiple(color, 0.5));
                writer.setColor(x, y, newColor);
            }
        }

        return  outputImage;
    }

    public Color colorMultiple(Color color1, double rate) {
        double r = color1.getRed() * rate,
                g = color1.getGreen() * rate,
                b = color1.getBlue() * rate;

        return new Color(r, g, b, color1.getOpacity());
    }

    public Color colorPlus(Color color1, Color color2) {
        double r = color1.getRed() + color2.getRed(),
                g = color1.getGreen() + color2.getGreen(),
                b = color1.getBlue() + color2.getBlue();

        System.out.printf("Before %f %f %f", r, g, b );
        r = r>1 ? 1 : r;
        g = g>1 ? 1 : g;
        b = b>1 ? 1 : b;

        System.out.printf("After %f %f %f", r, g, b );


        return new Color(r, g, b, color1.getOpacity());
    }
}
