package com.tankWar.game;

import com.tankWar.game.client.GameClient;
import com.tankWar.game.client.msg.InitMessage;
import com.tankWar.game.client.msg.Message;
import com.tankWar.game.client.msg.MoveMessage;
import com.tankWar.game.entity.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class GamePane extends BorderPane {
    // 游戏部分客户端
    GameClient client;

    // 用于绘制的组件
    Canvas canvas = new Canvas();
    GraphicsContext context = canvas.getGraphicsContext2D();

    // 游戏元素
//    List<Tank> tanks = new ArrayList<>();
    Tank[] tanks;
    Tank myTank; // 我的坦克
//    Tank testTank; // 测试坦克
    List<Bullet> bullets = new ArrayList<>(); // 子弹列表
    List<Building> buildings = new ArrayList<>(); // 建筑方块列表


    // 记录已经发生碰撞的方向
    Direction collideDir = Direction.INVALID;

    // 游戏逻辑控制参数
    boolean keyJPressed = false; // 是否按下发射键
    boolean hasFired = true; // 是否已处理开火

    // 构造函数
    public GamePane() {
        System.out.println("正在连接服务端");

        this.connectServer();

        this.init();
    }

    // 连接服务器
    void connectServer() {
        client = new GameClient();

        try {
            client.connect();
        }
        catch (TimeoutException e) {
            System.out.println("[Error] 连接超时!");
            return;
        }
        catch (IOException e) {
            System.out.println("[Error] 连接失败!");
            return;
        }

        // 接收初始消息
        InitMessage initMsg = client.receiveInitMsg();
        this.tanks = initMsg.getTanks();
        this.myTank = this.tanks[initMsg.getId()];

        // 连接成功后创建处理连接的线程
        Thread connectThread = new Thread(connectTask);
        connectThread.start();
    }

    // GamePane初始化函数
    void init() {
        // 载入地图
//        loadMap("/map/map.txt");
        // 载入测试地图
        loadMap("/map/test_map.txt");

        // 设置GamePane
        this.setWidth(Config.MapWidth);
        this.setHeight(Config.MapHeight);
        this.setStyle("-fx-background-color: Black");
        this.setPadding(new Insets(Config.MapPaddingSize));
        this.setCenter(canvas);

        // 设置Canvas
        this.canvas.requestFocus();
        this.canvas.setFocusTraversable(true);
        canvas.setWidth(Config.MapWidth);
        canvas.setHeight(Config.MapHeight);

        // 初始化玩家坦克
//        myTank = new Tank(Config.MapWidth / 2, Config.MapHeight / 2 - 75, 1);
//        testTank = new Tank(Config.MapWidth / 2, Config.MapHeight - 50, 2);
        /*
        初始化在线玩家坦克
        ...
        */
//        tanks.add(myTank);
//        tanks.add(testTank);

        // 创建显示游戏的线程
        Thread showThread = new Thread(showTask);
        showThread.start();

        // 创建处理元素移动和碰撞的线程
        Thread logicThread = new Thread(logicTask);
        logicThread.start();

        // 按下事件监听
        this.setOnKeyPressed(e -> {
            // 将按下的按键加入按键集合
            KeyCode code = e.getCode();
            // 若为方向键，则假如方向处理列表
            if (Utils.CheckCodeIsMove(code)) {
                Direction dir = Utils.DirMap.get(code);

                // 不同方向 || 已暂停: 则不再发送
                if((myTank.getDir() != dir || myTank.getIsStop() )&& dir != collideDir) {
                    // 向服务端发送移动消息
                    client.sendMove(dir);
                }

                myTank.setDirection(dir);
                myTank.setIsStop(false);
            }
            // 若为发射键，则修改状态为J键已按下未处理开火
            else if (code == KeyCode.J && !keyJPressed) {
                hasFired = false;
                keyJPressed = true;
            }
        });

        // 松开事件监听: 将松开的按键从集合中删除
        this.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();

            if (Utils.CheckCodeIsMove(code)) {
                // 如果松开的按键是当前的移动的方向 则停止
                if (myTank.getDir() == Utils.DirMap.get(code) && !myTank.getIsStop()) {
                    client.sendMove(Direction.CENTER);
                    myTank.setIsStop(true);
                }
            }
            else if (code == KeyCode.J && keyJPressed) {
                keyJPressed = false;
            }
        });
    }

    // 加载地图函数
    void loadMap(String MapFilePath) {
        int row = 0;
        int column = 0;
        InputStream inputStream = getClass().getResourceAsStream(MapFilePath);
        Scanner scanner = new Scanner(inputStream);
        row = 0;
        // 逐行读取地图文件内容
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // 处理每行的字符
            int i;
            for (i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                // 根据字符映射到地图元素
                buildings.add(new Building(i * Config.BlockSize + Config.BlockSize / 2, row * Config.BlockSize + Config.BlockSize / 2, c));
            }
            if (column < i) column = i;
            row++;
        }
        // 设置地图大小
        Config.BlockXNumber = column;
        Config.BlockYNumber = row;
        Config.MapWidth = Config.BlockXNumber * Config.BlockSize;
        Config.MapHeight = Config.BlockYNumber * Config.BlockSize;
    }

    // 显示游戏画面
    private void showGame() {
        // 每次显示都擦除整个界面，防止残影
        this.context.clearRect(0, 0, Config.MapWidth, Config.MapHeight);

        // 绘制坦克
        for (Tank tank: tanks) {
            context.drawImage(tank.getImage(), tank.getImageX(), tank.getImageY());
        }

        // 绘制子弹
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.move();
            context.drawImage(bullet.getImage(), bullet.getImageX(), bullet.getImageY());
            // 若子弹已死亡，则移除列表
            if (!bullet.isAlive()) {
                bullets.remove(i);
            }
        }
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
    Task<Void> connectTask = new Task<Void>() {
        // todo 处理移动请求
        private void handleMove(MoveMessage msg) {
            int id = msg.getId();
            Direction dir = msg.getDir();



        }

        @Override
        protected Void call() throws Exception {
            while (true) {
                Message msg = client.receiveStatusMsg();

                switch(msg.getType()) {
                    case Move -> handleMove((MoveMessage) msg);
                }

            }
        }
    };

    // 显示游戏界面Task
    Task<Void> showTask = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (true) {
                // 延时
                Thread.sleep(1000 / 60);
                // 使用runLater来多线程处理JavaFX组件
                Platform.runLater(() -> showGame());
            }
        }
    };

    // 游戏逻辑处理Task
    Task<Void> logicTask = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (true) {
                // 延时
                Thread.sleep(1000 / 60);

                // 处理本机坦克开火
                if (!hasFired) {
                    bullets.add(myTank.fire());
//                    bullets.add(testTank.fire());
                    hasFired = true;
                }

                // 处理移动按键输入
                for(Tank tank: tanks) {
                    // 如果不再移动 则不做处理
                    if (tank.getIsStop())
                        continue;

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
                        // 不再喷桩时则不喷桩
                        collideDir = Direction.INVALID;
                }
            }
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

    void processCollide() {
        // 处理子弹与建筑方块的碰撞
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = buildings.size() - 1; j >= 0; j--) {
                Building building = buildings.get(j);
                // 若方块可穿过以及与子弹碰撞
                if (!building.canGoThough() && bullet.isCollidingWith(building)) {
                    bullet.setAlive(false); // 子弹设置死亡
                    // 若方块是可击碎的，则设置方块死亡
                    if (building.isFragile()) {
                        building.setAlive(false);
                    }
                }

                // 处理坦克与子弹的碰撞
//                for (int j = bullets.size() - 1; j >= 0; j--) {
//                    Bullet bullet = bullets.get(j);
//                    if (bullet.id != tank.getID() && tank.isCollidingWith(bullet)) {
//                        bullet.setAlive(false);
//                    }
//                }
            }
        }
    }
}
