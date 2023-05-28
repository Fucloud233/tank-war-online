package com.tankWar.game.entity;

/*
    Bullet 子弹类
    由Tank发出，会与建筑方块和其他Tank发生碰撞
 */

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

public class Bullet extends Entity {
    // 坦克id
    public int id;
    double maxDistance;
    double startX,startY;

    private final int speed = Config.BulletSpeed;

    // 坦克不同方向照片
    private final Image bulletImageUp = new Image("/image/bulletUp.png");
    private final Image bulletImageDown = new Image("/image/bulletDown.png");
    private final Image bulletImageLeft = new Image("/image/bulletLeft.png");
    private final Image bulletImageRight = new Image("/image/bulletRight.png");

    // 子弹构造函数
    Bullet(Tank tank, Direction dir, double x, double y) {
        super(Config.BulletSize, Config.BulletSize);
        this.id = tank.getId();
        this.dir = dir;
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.maxDistance = Config.bulletMaxDistance;
        switch (dir) {
            case UP -> setImage(bulletImageUp);
            case DOWN -> setImage(bulletImageDown);
            case RIGHT -> setImage(bulletImageRight);
            case LEFT -> setImage(bulletImageLeft);
            default -> System.out.println("Direction error");
        }
    }

    // 子弹移动
    @Override
    public void move() {
        if (this.dir == Direction.LEFT) {
            x = x - speed;
        } else if (dir == Direction.RIGHT) {
            x = x + speed;
        } else if (dir == Direction.UP) {
            y = y - speed;
        } else if (dir == Direction.DOWN) {
            y = y + speed;
        }
        // 若超越地图边界或超过最大距离，子弹死亡
        double delta = Math.sqrt((x-startX)*(x-startX)+(y-startY)*(y-startY));
        if (this.x + this.width / 2 <= 0 || this.x - this.width / 2 >= Config.MapWidth || this.y + this.height / 2 <= 0 || this.y - this.height / 2 >= Config.MapHeight || delta >= maxDistance)
            this.alive = false;
    }

    // 检测子弹是否碰撞地图方块
    @Override
    public boolean isCollidingWith(Entity entity) {
        if (entity.isAlive()) {
            Rectangle boundBox = new Rectangle(this.x - this.width / 2, this.y - this.height / 2, this.width, this.height);
            return boundBox.intersects(entity.x - entity.width / 2, entity.y - entity.height / 2, entity.width, entity.height);
        }
        return false;
    }
}
