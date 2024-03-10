package com.tankWar.entity;

/*
    Bullet 子弹类
    由Tank发出，会与建筑方块和其他Tank发生碰撞
 */

import com.tankWar.utils.GameConfig;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;

public class Bullet extends Entity {
    // 坦克id
    public int id;
    double startX, startY;
    double maxDistance = GameConfig.bulletMaxDistance;
    private final int speed = GameConfig.BulletSpeed;

    // 记录所属父类坦克
    private Tank parentTank = null;

    // 子弹构造函数
    public Bullet(int id, Direction dir, double x, double y) {
        super(GameConfig.BulletSize, GameConfig.BulletSize);
        this.id = id;
        this.dir = dir;
        this.startX = this.x = x;
        this.startY = this.y = y;
    }

    // 子弹构造函数
    Bullet(Tank tank, Direction dir, double x, double y) {
        super(GameConfig.BulletSize, GameConfig.BulletSize);
        // 设置父类坦克
        this.parentTank = tank;

        this.id = tank.getId();
        this.dir = dir;
        this.startX = this.x = x;
        this.startY = this.y = y;
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
        // 不需要判断是否超越地图边界
        double delta = Math.sqrt((x-startX)*(x-startX)+(y-startY)*(y-startY));
        if (delta >= maxDistance)
            this.setAlive(false);
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

    // 子弹死亡后恢复子弹数量
    @Override
    public void setAlive(boolean alive) {
        // 防止重复设置
        if(parentTank!= null && (this.alive || !alive))
            this.parentTank.recoveryBullet();
        super.setAlive(alive);
    }

    @Override
    public Image getImage() {
       return BulletImg.ImageMap.get(this.dir);
    }
}

// 使用其他类来记录图像 降低数据和资源的耦合度
class BulletImg {
    // 子弹不同方向照片
    public static final HashMap<Direction, Image> ImageMap = new HashMap<Direction, Image>();

    static {
        try {
            ImageMap.put(Direction.UP, new Image("/image/bulletUp.png"));
            ImageMap.put(Direction.DOWN, new Image("/image/bulletDown.png"));
            ImageMap.put(Direction.LEFT, new Image("/image/bulletLeft.png"));
            ImageMap.put(Direction.RIGHT, new Image("/image/bulletRight.png"));
        }
        catch(Exception e) {
//            e.printStackTrace();
        }
    }
}