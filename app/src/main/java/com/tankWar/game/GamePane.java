package com.tankWar.game;

import com.tankWar.communication.msg.*;
import com.tankWar.game.client.GameClient;
import com.tankWar.game.entity.*;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.Socket;
import java.util.*;

public class GamePane extends HBox {
    // 与客户端交互
    GameClient client;
    // 用于获取Stage
    GamePane myself;
    // 记录游戏是否结束
    boolean isOver = false;

    // 1. 信息栏
    VBox InfoPane = new VBox();
    GameStatusPane statusPane = new GameStatusPane();
    // 2. 用于绘制的组件 (JavaFX相关内容)
    Canvas canvas = new Canvas();
    GraphicsContext context = canvas.getGraphicsContext2D();

    // 游戏实体
    // 当前选择地图信息
    GameMap map = new GameMap();
    // 所有坦克/我的坦克
    Tank[] tanks = new Tank[0];
    Tank myTank;
    // 子弹列表
    Vector<Bullet> bullets = new Vector<>();
    // 建筑方块列表
    List<Building> buildings = new ArrayList<>();

    // 记录已经发生碰撞的方向 (防止发生重碰撞)
    Direction collideDir = Direction.INVALID;
    // 游戏逻辑控制参数
    boolean keyJPressed = false; // 是否按下发射键

    // 构造函数
    public GamePane() {
//        this.initEntity();
        this.initPane();
        this.initAction();
    }

    // 带服务端端口号的构造函数，用于指定该GamePane所连接的端口号
    public GamePane(Socket clientSocket) {
        myself=this;
        this.initEntity(clientSocket);
        this.initPane();
        this.initAction();
    }

    // 连接服务器
    void initEntity(Socket clientSocket) {
        System.out.println("[info] socket:"+clientSocket);
        client = new GameClient();
        // 指定服务端的端口号
        client.setSocket(clientSocket);
        // 连接成功后创建处理连接的线程
        Thread connectThread = new Thread(connectTask);
        connectThread.start();
    }

    // GamePane初始化函数
    void initPane() {
        // 设置InfoPane
        Label titleLabel = new Label("游戏状态");
        titleLabel.setStyle("-fx-font-style: BOLD");
        titleLabel.setStyle("-fx-font-family: 黑体;");
        titleLabel.setStyle("-fx-font-size: 28px;");

        Separator separator = new Separator();

        InfoPane.setSpacing(5);
        InfoPane.setPrefSize(150, Config.MapMaxHeight);
        InfoPane.setPadding(new Insets(Config.MapPaddingSize));
        InfoPane.getChildren().addAll(titleLabel, separator, statusPane);

        // 设置Canvas
        this.canvas.requestFocus();
        this.canvas.setFocusTraversable(true);
        canvas.setWidth(Config.MapMaxWidth);
        canvas.setHeight(Config.MapMaxHeight);

        // 设置GamePane
        this.setWidth(Config.MapMaxWidth);
        this.setHeight(Config.MapMaxHeight);
//        this.setStyle("-fx-background-color: Black");
        this.setPadding(new Insets(Config.MapPaddingSize));

        // 添加子Pane
        this.getChildren().add(canvas);
        this.getChildren().add(InfoPane);

        // 创建显示游戏的线程
        Thread showThread = new Thread(showTask);
        showThread.start();
    }

    // 初始化游戏控制
    void initAction() {
        // 按下事件监听
        this.setOnKeyPressed(e -> {
            // 将按下的按键加入按键集合
            KeyCode code = e.getCode();
            // 若为方向键，则假如方向处理列表
            if (Utils.CheckCodeIsMove(code)) {
                Direction dir = Utils.DirMap.get(code);

                // (不同方向 || 停止移动) & 移动方向不是碰撞方向
                if((myTank.getDir() != dir || myTank.getIsStop() )&& dir != collideDir) {
                    // 向服务端发送移动消息
                    client.sendMoveMsg(dir, myTank.getX(), myTank.getY());
                }

                myTank.setDirection(dir);
                myTank.setIsStop(false);
            }
            // 若为发射键，则修改状态为J键已按下未处理开火
            else if (code == KeyCode.J && !keyJPressed) {
                // 如果发出的子弹数量超出上限 返回null 此时不添加进去
                Bullet bullet = myTank.fire();
                if(bullet != null) {
                    bullets.add(bullet);
                    // 告知服务端发射子弹
                    client.sendShootMsg(bullet.getDir(), bullet.getX(), bullet.getY());
                }

                keyJPressed = true;
            }
        });

        // 松开事件监听: 将松开的按键从集合中删除
        this.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();

            if (Utils.CheckCodeIsMove(code)) {
                // 如果松开的按键是当前的移动的方向 则停止
                if (myTank.getDir() == Utils.DirMap.get(code) && !myTank.getIsStop()) {
                    // 告知服务端停止移动
                    client.sendMoveMsg(Direction.CENTER, myTank.getX(), myTank.getY());
                    myTank.setIsStop(true);
                }
            }
            else if (code == KeyCode.J && keyJPressed) {
                keyJPressed = false;
            }
        });

        // 创建处理元素移动和碰撞的线程
        Thread logicThread = new Thread(logicTask);
        logicThread.start();
    }

    void initEntity(int mapId) {
        // 1. 清除子弹
        bullets.clear();

        // 2. 加载地图 (包括建筑物信息+坦克信息)
        // 如果地图编号改变则重新加载
        if(mapId != this.map.getId()) {
            if(!this.map.loadMap(mapId))
                System.out.println("[error] 地图加载失败");
        }

        // 设置建筑物信息
        this.buildings = map.getBuildings();

        // 设置坦克初始初始化信息
        this.tanks = map.getTanks();

        // 3. 设置自己的坦克 (客户端保存玩家id)
        myTank = tanks[client.getId()];
    }

    // 显示游戏画面
    private void showGame() {
        // 每次显示都擦除整个界面，防止残影
        this.context.clearRect(0, 0, Config.MapMaxWidth, Config.MapMaxHeight);

        // 绘制黑色背景
        this.context.setStroke(Color.BLACK);
        this.context.fillRect(0, 0, Config.MapMaxWidth, Config.MapMaxHeight);

        // 绘制坦克
        for (Tank tank: tanks)
            if(tank.isAlive())
                context.drawImage(tank.getImage(), tank.getImageX(), tank.getImageY());

        // 绘制子弹
        for (Bullet bullet : bullets)
            context.drawImage(bullet.getImage(), bullet.getImageX(), bullet.getImageY());

        // 绘制建筑方块
        for (int i = buildings.size() - 1; i >= 0; i--) {
            Building building = buildings.get(i);
            context.drawImage(building.getImage(), building.getImageX(), building.getImageY());
            // 若建筑方块已死亡，则移除列表
            if (!building.isAlive()) {
                buildings.remove(i);
            }
        }
    }

    // 处理连接Task
    Task<Void> connectTask = new Task<>() {
        @Override
        protected Void call() throws InterruptedException {
            while (!isOver) {
                // 延时
                Thread.sleep(Config.RefreshRate);
                // 尝试接收消息
                Message msg = client.receiveStatusMsg();

                // 如果没有接收到消息则跳过
                if(msg == null) {
//                    System.out.println("为接收到消息 " + count);
                    continue;
                }

                switch(msg.getType()) {
                    case Move -> handleMove((MoveMsg) msg);
                    case Shoot -> handleShoot((ShootMsg) msg);
                    case Init ->  handleInit((InitMsg) msg);
                    case Reset -> handleReset((ResetMsg) msg);
                    case Over -> handleOver((OverMsg) msg);
                    default -> {
                    }
                }
            }
            return null;
        }

        // 处理移动请求
        private void handleMove(MoveMsg msg) {
            int id = msg.getId();
            Direction dir = msg.getDir();

            if(dir != Direction.CENTER) {
                tanks[id].setDirection(dir);
                tanks[id].setIsStop(false);
            } else {
                tanks[id].setIsStop(true);
            }

            // 重置坦克的坐标信息
            tanks[id].setX(msg.getX());
            tanks[id].setY(msg.getY());
        }

        // 处理服务端发来的其他坦克开火请求
        private void handleShoot(ShootMsg msg) {
            int id = msg.getId();
            Direction dir = msg.getDir();
            double x = msg.getX(), y = msg.getY();

            bullets.add(new Bullet(id, dir, x, y));
        }

        // 处理服务端初始化游戏请求
        private void handleInit(InitMsg msg) {
            // 修改状态栏信息
            Platform.runLater(()->{
                statusPane.setTotalGameNum(msg.getTotalGameNum());
                statusPane.setCurGameNum(1);
                statusPane.setTotalPlayerNum(msg.getPlayerNum());
                statusPane.setRestPlayerNum(msg.getPlayerNum());
            });

            initEntity(msg.getMapId());
        }

        private void handleReset(ResetMsg msg) {
            // 修改状态栏信息
            Platform.runLater(()-> {
                statusPane.setCurGameNum(msg.getCurGameNum());
                statusPane.setTotalPlayerNum(msg.getPlayerNum());
                statusPane.setRestPlayerNum(msg.getPlayerNum());
            });

            initEntity(msg.getMapId());
        }

        private void handleOver(OverMsg msg) {
            // 显示结束页面
            isOver = true;

            Platform.runLater(()->{
                OverDialog dialog = new OverDialog(msg.getScores());
                dialog.display();
                Stage stage = (Stage) myself.getScene().getWindow();
                stage.close();
            });
        }
    };

    // 显示游戏界面Task
    Task<Void> showTask = new Task<>() {
        @Override
        protected Void call() throws Exception {
        while (!isOver) {
            // 延时
            Thread.sleep(Config.RefreshRate);
            // 使用runLater来多线程处理JavaFX组件
            Platform.runLater(() -> showGame());
        }

        return null;
        }
    };

    // 游戏逻辑处理Task
    Task<Void> logicTask = new Task<>() {
        @Override
        protected Void call() throws Exception {
        while (!isOver) {
            // 延时
            Thread.sleep(Config.RefreshRate);

            // 处理移动按键输入
            for(Tank tank: tanks) {
                // 如果不再移动或移动方向与碰撞方向相同 则不做处理
                if (tank.getIsStop() || tank.getDir() == collideDir)
                    continue;

                // 移动并记录先前位置
                double x = tank.getX(), y = tank.getY();
                tank.move();

                // 如果发生碰撞 则归位并暂停
                if(processTankCollide(tank)) {
                    tank.setX(x); tank.setY(y);
                    tank.setIsStop(true);

                    // 当该方向发生碰撞时 则记录之
                    if(tank == myTank) {
                        collideDir = myTank.getDir();
                    }
                }
                else
                    // 不再碰撞时则不碰撞
                    collideDir = Direction.INVALID;
            }

            // 处理子弹发射
            for (int i=bullets.size()-1; i>=0; i--) {
                Bullet bullet = bullets.get(i);
                // 若子弹已死亡，则移除列表
                if (!bullet.isAlive()) {
                    bullets.remove(i);
                }

                bullet.move();
                if(processBulletCollide(bullet)) {
                    bullet.setAlive(false);

                    // 减少玩家数量
                    Platform.runLater(()->statusPane.decResetPlayerNum());
                }
            }
        }

        return null;
        }
    };

    // 处理碰撞函数
    boolean processTankCollide(Tank tank) {
        // 获得坦克
        // 坦克死亡则无需判断
        if (tank == null || !tank.isAlive() )
            return false;

        // 处理坦克与子弹/建筑方块的碰撞
        boolean isCollide = false;

        // 处理坦克与建筑方块的碰撞
        for (int j = buildings.size() - 1; j >= 0; j--) {
            Building building = buildings.get(j);
            if (!building.canGoThough() && tank.isCollidingWith(building)) {
                isCollide = true;
            }
        }

        // 处理坦克与坦克的碰撞
        for (Tank tankCollide: tanks) {
            if (tank == tankCollide) continue;
            if (tank.isCollidingWith(tankCollide)) {
                isCollide = true;
            }
        }

        return isCollide;
    }

    boolean processBulletCollide(Bullet bullet) {
        // 处理子弹与建筑方块的碰撞
        for (Building building: buildings) {
            // 若方块可穿过以及与子弹碰撞
            if (!building.canGoThough() && bullet.isCollidingWith(building)) {
                // 若方块是可击碎的，则设置方块死亡
                if (building.isFragile()) {
                    building.setAlive(false);
                }
                return true; // 子弹设置死亡
            }
        }
        // 处理坦克与子弹的碰撞
        for (Tank tank: tanks) {
            if (bullet.id != tank.getId() && bullet.isCollidingWith(tank)) {
                if(tank.getId() == myTank.getId())
                    client.sendDeadMsg();

                tank.setAlive(false);
                return true;
            }
        }

        return false;
    }
}
