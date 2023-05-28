package com.tankWar.game.entity;

/*
    Tank 坦克类
 */

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import javax.swing.*;

public class Tank extends Entity {
    // id 玩家编号
    public int id;
    // 坦克是否可以移动
    public boolean canMove = true;
    // 坦克速度
    private int speed = Config.TankSpeed;
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
            case UP:
                setDirection(Direction.UP);
                break;
            case DOWN:
                setDirection(Direction.DOWN);
                break;
            case LEFT:
                setDirection(Direction.LEFT);
                break;
            case RIGHT:
                setDirection(Direction.RIGHT);
                break;
            default:
                System.out.println("Direction error");
        }
    }

    // 设置方向
    public void setDirection(Direction dir) {

        switch (dir) {
            case LEFT:
                this.dir = Direction.LEFT;
                setImage(tankImageLeft);
                this.width = Config.TankWidth;
                this.height = Config.TankHeight;
                break;
            case RIGHT:
                this.dir = Direction.RIGHT;
                setImage(tankImageRight);
                this.width = Config.TankWidth;
                this.height = Config.TankHeight;
                break;
            case UP:
                this.dir = Direction.UP;
                setImage(tankImageUp);
                this.width = Config.TankHeight;
                this.height = Config.TankWidth;
                break;
            case DOWN:
                this.dir = Direction.DOWN;
                setImage(tankImageDown);
                this.width = Config.TankHeight;
                this.height = Config.TankWidth;
                break;
            default:
                System.out.println("Direction error");
        }

    }

    // 坦克移动函数
    public void move(Direction dir) {
        if (dir != this.dir) { // 方向不同，则转向
            switch (dir) {
                case LEFT:
                    setDirection(Direction.LEFT);
                    break;
                case RIGHT:
                    setDirection(Direction.RIGHT);
                    break;
                case UP:
                    setDirection(Direction.UP);
                    break;
                case DOWN:
                    setDirection(Direction.DOWN);
                    break;
                default:
                    System.out.println("Direction error");
            }
        } else if (this.canMove) { // 方向相同，则移动
            switch (dir) {
                case LEFT:
                    if (x - this.width / 2 - speed >= 0) {
                        x = x - speed;
                    }
                    break;
                case RIGHT:
                    if (x + this.width / 2 + speed <= Config.MapWidth) {
                        x = x + speed;
                    }
                    break;
                case UP:
                    if (y - this.height / 2 - speed >= 0) {
                        y = y - speed;
                    }
                    break;
                case DOWN:
                    if (y + this.height / 2 + speed <= Config.MapHeight) {
                        y = y + speed;
                    }
                    break;
                default:
                    System.out.println("Direction error");
            }
        }

    }

    public int getId() {
        return id;
    }

    // 发射子弹
    public Bullet fire() {
//        System.out.println("fire!");
        Bullet bullet = null;
        switch (this.dir) {
            case UP:
                bullet = new Bullet(this, this.dir, this.x, this.y - Config.TankHeight / 2 - Config.BulletSize / 2);
                break;
            case DOWN:
                bullet = new Bullet(this, this.dir, this.x, this.y + Config.TankHeight / 2 + Config.BulletSize / 2);
                break;
            case LEFT:
                bullet = new Bullet(this, this.dir, this.x - Config.TankHeight / 2 - Config.BulletSize / 2, this.y);
                break;
            case RIGHT:
                bullet = new Bullet(this, this.dir, this.x + Config.TankHeight / 2 + Config.BulletSize / 2, this.y);
                break;
            default:
                System.out.println("Direction error");
        }

        return bullet;
    }

    // 碰撞检测
    public boolean collideWith(Entity entity) {
        if (entity.isAlive()) {
            Rectangle boundBox = null;
            float delta = (Config.TankWidth - Config.TankHeight) / 2;
            switch (this.dir) {
                case UP:
                    boundBox = new Rectangle(this.x - this.width / 2, this.y - this.height / 2, this.width, this.height - delta);
                    break;
                case DOWN:
                    boundBox = new Rectangle(this.x - this.width / 2, this.y - this.height / 2 + delta, this.width, this.height - delta);
                    break;
                case LEFT:
                    boundBox = new Rectangle(this.x - this.width / 2, this.y - this.height / 2, this.width - delta, this.height);
                    break;
                case RIGHT:
                    boundBox = new Rectangle(this.x - this.width / 2 + delta, this.y - this.height / 2, this.width - delta, this.height);
                    break;
            }
            if (boundBox.intersects(entity.x - entity.width / 2, entity.y - entity.height / 2, entity.width, entity.height)) {
//                System.out.println("collide!");
                return true;
            }
            return false;
        }
        return false;
    }
}
