package com.tankWar.game.entity;

/*
    Tank 坦克类
 */

import com.tankWar.game.Config;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;

public class Tank extends Entity {
    // id 玩家编号
    private int id;
    // 坦克是否可以移动
    private boolean isStop = true;
    // 记录坦克当前的子弹数量
    private int bulletNum = Config.TankMaxBulletNum;


    // tank构造函数(随机方向)
    // 不能直接删除
    public Tank(int id, double x, double y) {
        super(Config.TankWidth, Config.TankHeight, x, y);
        this.id = id;
        // 随机坦克方向
        double random = Math.random();
        if (random <= 0.25) {
            setDirection(Direction.UP);
        } else if (random <= 0.5) {
            setDirection(Direction.DOWN);
        } else if (random <= 0.75) {
            setDirection(Direction.LEFT);
        } else {
            setDirection(Direction.RIGHT);
        }
    }

    // tank构造函数(带方向)
    public Tank(int id, double x, double y, Direction dir) {
        super(Config.TankWidth, Config.TankHeight, x, y);
        this.id = id;
        this.setDirection(dir);
    }

    // 复制构造函数
    public Tank(Tank tank) {
        this(tank.getId(), tank.getX(), tank.getY(), tank.getDir());
    }

    // 设置方向
    public void setDirection(Direction dir) {
        this.dir = dir;

        // 设置渲染图片方向的同时，重新设置长与宽
        // 图片由Map映射 不需要单独设置
        switch (dir) {
            case LEFT, RIGHT -> {
                this.width = Config.TankWidth;
                this.height = Config.TankHeight;
            }
            case UP, DOWN -> {
                this.width = Config.TankHeight;
                this.height = Config.TankWidth;
            }
            default -> System.out.println("[error] Direction error");
        }
    }

    // 坦克移动函数
    public void move() {
        if(this.isStop)
            return;

        // 根据当前方向和速度移动
        int speed = Config.TankSpeed;
        switch (dir) {
            case LEFT -> x -= speed;
            case RIGHT -> x += speed;
            case UP -> y -= speed;
            case DOWN -> y +=  speed;
            default -> System.out.println("[error] Direction error");
        }
    }

    // 发射子弹
    public Bullet fire() {
        // 验证子弹是否发射
        if(this.bulletNum <= 0) {
            return null;
        }

        // 简化创建子弹的代码
        double x = this.x, y = this.y;
        // 枪口到子弹中心点位置
        double distance = + Config.TankHeight / 2 + Config.BulletSize / 2;
        switch (this.dir) {
            case UP ->  y = this.y - distance;
            case DOWN -> y = this.y + distance;
            case LEFT -> x = this.x - distance;
            case RIGHT -> x = this.x + distance;
            default -> System.out.println("[error] Direction error");
        }

        Bullet bullet = new Bullet(this, this.dir, x, y);
        bulletNum -= 1;

        return bullet;
    }

    // 恢复最大子弹数量
    public void recoveryBullet() {
        if(bulletNum < Config.TankMaxBulletNum)
            this.bulletNum ++;
    }

    // 碰撞检测
    public boolean isCollidingWith(Entity entity) {
        if (entity.isAlive()) {
            Rectangle boundBox = null;
            // 将坦克尾部从碰撞域中减去
            float delta = (Config.TankWidth - Config.TankHeight) / 2;
            switch (this.dir) {
                case UP ->
                        boundBox = new Rectangle(this.x - this.width / 2, this.y - this.height / 2, this.width, this.height - delta);
                case DOWN ->
                        boundBox = new Rectangle(this.x - this.width / 2, this.y - this.height / 2 + delta, this.width, this.height - delta);
                case LEFT ->
                        boundBox = new Rectangle(this.x - this.width / 2, this.y - this.height / 2, this.width - delta, this.height);
                case RIGHT ->
                        boundBox = new Rectangle(this.x - this.width / 2 + delta, this.y - this.height / 2, this.width - delta, this.height);
            }
            assert boundBox != null;
            return boundBox.intersects(entity.x - entity.width / 2, entity.y - entity.height / 2, entity.width, entity.height);
        }
        return false;
    }

    @Override
    public Image getImage() {
        return TankImg.ImageMap.get(this.dir)[id];
    }

    public int getId() {
        return id;
    }

    public boolean getIsStop() {
        return isStop;
    }

    public void setIsStop(boolean isStop) {
        this.isStop = isStop;
    }
}

// 使用其他类来记录图像 降低数据和资源的耦合度
class TankImg {
    // 坦克不同方向照片
    public static final HashMap<Direction, Image[]> ImageMap = new HashMap<>();

    // 记录坦克
    public enum TankColor {
        Green(0, Color.GREEN),
        White(1, Color.WHITE),
        Red(2, Color.RED),
        Blue(3, Color.BLUE);

        final int value;
        final Color color;

        TankColor(int value, Color color) {
            this.value = value;
            this.color = color;
        }

        int getValue() {
            return value;
        }

        // 获得颜色
        Color getColor() {
            return this.color;
        }

        // 用于修改颜色
        Color modify(Color color) {
            double r = color.getRed(), g = color.getGreen(), b = color.getBlue(),
                    o = color.getOpacity();

            switch (this) {
                case Green -> {
                    return new Color(r, g, b, o);
                }
                case White -> {
                    return new Color(g, g, g, o);
                }
                case Red -> {
                    return new Color(g, r, b, o);
                }
                case Blue -> {
                    return new Color(r, b, g, o);
                }
                default -> {
                    return color;
                }
            }
        }
    }

    static {
        try {
            loadImages(Direction.UP);
            loadImages(Direction.DOWN);
            loadImages(Direction.LEFT);
            loadImages(Direction.RIGHT);

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadImages(Direction dir) {
        Image[] images = new Image[Config.MaxPlayerNumber];
        Image sourceImage = new Image("/image/tank/"+dir.toString()+".png");
        for(int i=0; i<images.length; i++) {
            images[i] = modifyImage(sourceImage, TankColor.values()[i]);
        }

        ImageMap.put(dir, images);
    }

    private static Image modifyImage(Image sourceImage, TankColor color) {
        final int w = (int) sourceImage.getWidth();
        final int h = (int) sourceImage.getHeight();
        final WritableImage outputImage = new WritableImage(w, h);
        final PixelWriter writer = outputImage.getPixelWriter();
        final PixelReader reader = sourceImage.getPixelReader();

        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                writer.setColor(x, y, color.modify(reader.getColor(x, y)));
        return outputImage;
    }
}