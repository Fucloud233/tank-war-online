package com.tankWar.game.entity;

/*
    Tank 坦克类
 */

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

public class Tank extends Entity {
    // id 玩家编号
    public int id;
    // 坦克是否可以移动
    public boolean canMove = true;
    // 坦克速度
    private final int speed = Config.TankSpeed;
    // 坦克不同方向照片
    private final Image tankImageUp = new Image("/image/tankUp.png");
    private final Image tankImageDown = new Image("/image/tankDown.png");
    private final Image tankImageLeft = new Image("/image/tankLeft.png");
    private final Image tankImageRight = new Image("/image/tankRight.png");

    // tank构造函数(随机方向)
    public Tank(double x, double y, int id) {
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
    public Tank(double x, double y, Direction dir, int id) {
        super(Config.TankWidth, Config.TankHeight, x, y);
        this.id = id;
        switch (dir) {
            case UP -> setDirection(Direction.UP);
            case DOWN -> setDirection(Direction.DOWN);
            case LEFT -> setDirection(Direction.LEFT);
            case RIGHT -> setDirection(Direction.RIGHT);
            default -> System.out.println("Direction error");
        }
    }

    // 获取坦克ID
    public int getId() {
        return id;
    }

    // 设置方向
    public void setDirection(Direction dir) {
        // 设置渲染图片方向的同时，重新设置长与宽
        switch (dir) {
            case LEFT -> {
                this.dir = Direction.LEFT;
                setImage(tankImageLeft);
                this.width = Config.TankWidth;
                this.height = Config.TankHeight;
            }
            case RIGHT -> {
                this.dir = Direction.RIGHT;
                setImage(tankImageRight);
                this.width = Config.TankWidth;
                this.height = Config.TankHeight;
            }
            case UP -> {
                this.dir = Direction.UP;
                setImage(tankImageUp);
                this.width = Config.TankHeight;
                this.height = Config.TankWidth;
            }
            case DOWN -> {
                this.dir = Direction.DOWN;
                setImage(tankImageDown);
                this.width = Config.TankHeight;
                this.height = Config.TankWidth;
            }
            default -> System.out.println("Direction error");
        }
    }

    // 坦克移动函数
    public void move(Direction dir) {
        if (dir != this.dir) { // 方向不同，则转向
            switch (dir) {
                case LEFT -> setDirection(Direction.LEFT);
                case RIGHT -> setDirection(Direction.RIGHT);
                case UP -> setDirection(Direction.UP);
                case DOWN -> setDirection(Direction.DOWN);
                default -> System.out.println("Direction error");
            }
        } else if (this.canMove) { // 若可移动且方向相同，则移动
            switch (dir) {
                case LEFT -> {
                    if (x - this.width / 2 - speed >= 0) {
                        x = x - speed;
                    }
                }
                case RIGHT -> {
                    if (x + this.width / 2 + speed <= Config.MapWidth) {
                        x = x + speed;
                    }
                }
                case UP -> {
                    if (y - this.height / 2 - speed >= 0) {
                        y = y - speed;
                    }
                }
                case DOWN -> {
                    if (y + this.height / 2 + speed <= Config.MapHeight) {
                        y = y + speed;
                    }
                }
                default -> System.out.println("Direction error");
            }
        }
    }

    // 发射子弹
    public Bullet fire() {
        Bullet bullet = null;
        switch (this.dir) {
            case UP ->
                    bullet = new Bullet(this, this.dir, this.x, this.y - Config.TankHeight / 2 - Config.BulletSize / 2);
            case DOWN ->
                    bullet = new Bullet(this, this.dir, this.x, this.y + Config.TankHeight / 2 + Config.BulletSize / 2);
            case LEFT ->
                    bullet = new Bullet(this, this.dir, this.x - Config.TankHeight / 2 - Config.BulletSize / 2, this.y);
            case RIGHT ->
                    bullet = new Bullet(this, this.dir, this.x + Config.TankHeight / 2 + Config.BulletSize / 2, this.y);
            default -> System.out.println("Direction error");
        }
        return bullet;
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
}
