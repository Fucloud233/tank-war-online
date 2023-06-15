package com.tankWar.lobby;


import com.tankWar.communication.Communicate;
import com.tankWar.game.GamePane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;


public class Client extends Stage {

    // 连接相关的 由登录页面进行传入初始值
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    //聊天框界面的UI
    private TextField txtTalk;
    private ComboBox<String> listOnline;
    private TextArea txtViewTalk;
    private VBox container;
    private StringTokenizer st;

    private CreateRoomWindow roomWindow;  //创建房间
    private SelectRoomWindow selectRoomWindow; //选择房间密码弹窗
    private GameWaitWindow gameWaitWindow; // 房间等待
    private String roomId;//选中的房间号
    private HBox selectedHBox;//选中的房间条目
    private boolean isHBoxSelected = false;//判断是否有选择房间条目

    private final String username;   //登录时传过来的用户名
    private final String account;  //用户的账号
    private Scene lobbyScene; //游戏大厅的场景，方便切换场景
    private Stage primaryStage;

    boolean gameStart = false;


    public Client(String nickname, String account, Socket socket, DataInputStream in, DataOutputStream out) {//因为加上了昵称，所以修改了下传参
        username = nickname;
        this.account = account;
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public void RunClient() throws IOException {
        //绑定
        //聊天室界面
        primaryStage = new Stage();
        primaryStage.setTitle("游戏大厅");

        BorderPane borderPane = new BorderPane();
        //设置迷彩背景
        borderPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #4D774E, #9C8B56, #614D79);");
        //游戏大厅的聊天框部分
        txtTalk = new TextField();   //编辑发送内容
        // 设置聊天框的输入框样式
        txtTalk.setStyle("-fx-background-color: #494f3c; -fx-text-fill: white;");
        txtTalk.setStyle("-fx-prompt-text-fill: white;");
        txtViewTalk = new TextArea();   //查看聊天内容
        // 设置txtViewTalk的样式
        txtViewTalk.setStyle("-fx-control-inner-background: #494f3c; -fx-text-fill: white;-fx-font-size: 15");
        Button btnTalk = new Button("发送");  //发送按钮
        listOnline = new ComboBox<>();  //选择发送的对象
        listOnline.getItems().add("All");  //添加在线人员列表
        txtViewTalk.setEditable(false);  //禁止编辑
        //放置输入聊天内容的盒子
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(10));
        hBox.getChildren().addAll(new Label("聊天内容:"), txtTalk, new Label("发送给:"), listOnline, btnTalk);
        //放置接收到的聊天内容的盒子
        VBox vBox = new VBox();
        vBox.getChildren().add(txtViewTalk); //放入聊天内容
        vBox.getChildren().add(hBox); //放入发送框

        //带滚轮的房间列表
        container = new VBox();
        container.setStyle("-fx-background-color: #494f3c;-fx-control-inner-background:green");

        container.setMinHeight(380); // 设置容器的固定高度
        ScrollPane scrollPane = new ScrollPane(container);//房间列表
        scrollPane.setFitToWidth(true);


        //创建房间按钮
        //游戏大厅的UI
        Button newRoomBtn = new Button("创建房间");
        newRoomBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");

        //加入房间按钮
        Button enterRoomBtn = new Button("加入房间");
        enterRoomBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");

        //装填按钮的盒子
        HBox ButtonBox = new HBox();
        ButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        ButtonBox.setSpacing(25);  //按钮间距
        ButtonBox.setPadding(new Insets(10));
        ButtonBox.getChildren().addAll(newRoomBtn, enterRoomBtn);
        // 设置 ButtonBox 样式
        ButtonBox.setStyle("-fx-border-color: black; -fx-border-width: 3px;");

        //将按钮与聊天框放置在一起
        VBox BottomBox = new VBox();
        BottomBox.getChildren().add(ButtonBox);
        BottomBox.getChildren().add(vBox);

        //最后将组件排布在borderPane上
        borderPane.setCenter(scrollPane);
        borderPane.setBottom(BottomBox);

        lobbyScene = new Scene(borderPane, 800, 700);
        primaryStage.setScene(lobbyScene);
        primaryStage.setResizable(false); // 禁用窗口大小调整
        //显示界面
        btnTalk.setDisable(false);
        //创建一个线程来处理事件
        new Thread(new ClientThread()).start();
        Communicate.send(socket, "init|online");
        primaryStage.show();

        //加入房间的按钮
        enterRoomBtn.setOnAction(e -> {
            if (selectedHBox!=null && isHBoxSelected==true){
                System.out.println("roomid:"+roomId);
                //向服务端传选择的房间内容
                Communicate.send(socket, "Select room|" + roomId);
            } else {
                new Alert(Alert.AlertType.WARNING, "请选择房间！").showAndWait();
            }
        });

        // 聊天框中的按钮事件
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                //获取用户输入的账号
                Communicate.send(socket, "talk|" + txtTalk.getText() + "|" + username + "|" + listOnline.getValue());
                if (listOnline.getValue()==null) {
                    //没有选择和谁说话 默认为全体成员
                    Communicate.send(socket, "talk|" + txtTalk.getText() + "|" + username + "|" + "All");
                }
                else{
                    Communicate.send(socket, "talk|" + txtTalk.getText() + "|" + username + "|" + listOnline.getValue());
                }
                txtTalk.clear();
            }
        });
        //创建房间按钮的事件
        newRoomBtn.setOnAction(e -> {
            //获取用户输入的账号
            try {
                //创建一个新的房间
                gameWaitWindow = new GameWaitWindow(socket, username, account, primaryStage, lobbyScene);
                ///////////////////////////new一个新的创建房间窗口  设置房间的信息////////////////////////
                roomWindow = new CreateRoomWindow(socket, username, account, gameWaitWindow);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            //显示页面
            roomWindow.ShowWindow();
        });
    }

    class ClientThread implements Runnable {
        //这用来创建每一条房间的消息，显示在聊天框上面
        public HBox createHBox(String roomNum, String id, String roomName, String hostName, String enterNum, String userNum, String status) {
            HBox hBox = new HBox();
            hBox.setStyle("-fx-border-color: black; -fx-border-width: 1px");
            hBox.setAlignment(Pos.CENTER);
            hBox.setPadding(new Insets(10));
            hBox.setSpacing(20);
            hBox.setId(roomNum);//用房间号做唯一标识
            Label Id = new Label(id);
            Id.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            Label roomLabel = new Label("房间名：" + roomName);
            roomLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            Label hostLabel = new Label("房主：" + hostName);
            hostLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            Label enterNumLabel = new Label("房间人数：" + enterNum);
            enterNumLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            Label userNumLabel = new Label("人数上限：" + userNum);
            userNumLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            Label statusLabel = new Label("房间状态：" + status);
            statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

            hBox.getChildren().addAll(Id, roomLabel, hostLabel, enterNumLabel, userNumLabel, statusLabel);
            hBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5);" +
                    "-fx-border-color: gray;" +
                    "-fx-border-width: 2px;" +
                    "-fx-padding: 10px;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.5), 10, 0, 0, 0);");

            //设置hbox的选中事件
            hBox.setOnMouseClicked(event -> {
                if (selectedHBox != null) {
                    selectedHBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5);" +
                            "-fx-border-color: gray;" +
                            "-fx-border-width: 2px;" +
                            "-fx-padding: 10px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.5), 10, 0, 0, 0);");
                }
                roomId = hBox.getId();
                selectedHBox = hBox;
                hBox.setStyle("-fx-background-color: #9C8B56;" +
                        "-fx-border-color: #06280b;" +
                        "-fx-border-width: 2px;" +
                        "-fx-padding: 10px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.5), 10, 0, 0, 0);");
                isHBoxSelected = true;
            });

            return hBox;
        }

        public void run() {
            while (!gameStart) {
                try {
                    String strReceive = Communicate.receive(socket);
                    System.out.println(strReceive);
                    st = new StringTokenizer(strReceive, "|");
                    String strKey = st.nextToken();
                    //截取消息 显示对应的内容
                    switch (strKey) {
                        //游戏大厅部分聊天框的聊天内容
                        case "talk" -> {
                            String strTalk = st.nextToken();
                            //显示聊天的内容
                            Platform.runLater(() -> txtViewTalk.appendText("\n" + strTalk));
                        }
                        //游戏大厅部分聊天框中在线用户的更新
                        case "online" -> Platform.runLater(() -> {
                            listOnline.getItems().clear();
                            listOnline.getItems().add("All");
                            while (st.hasMoreTokens()) {
                                String strOnline = st.nextToken();
                                listOnline.getItems().add(strOnline);
                            }
                        });
                        //登陆失败提示
                        case "warning" -> {
                            String strWarning = st.nextToken();
                            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, strWarning).showAndWait());
                        }
                        //创建房间的信息
                        case "Create" -> {
                            //不断获取传来的信息  判断是否创建失败
                            while (st.hasMoreTokens()) {
                                String strCreate = st.nextToken();
                                if (strCreate.equals("Failed")) {
                                    Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "你已经创建过房间了！").showAndWait());
                                } else {
                                    Platform.runLater(() -> {
                                        roomWindow.CloseWindow();
                                    });
                                }
                            }
                        }
                        //选择加入房间
                        case "select room" -> {
                            String strSelect = st.nextToken();
                            if (strSelect.equals("failed")) {
                                Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "无法进入房间！").showAndWait());
                            } else if (strSelect.equals("success")) {//进入游戏房间
                                Platform.runLater(() -> {
                                    if (selectRoomWindow != null && selectRoomWindow.isShowing()) {//如果有输入密码的框，就可以关掉了
                                        selectRoomWindow.CloseWindow();
                                    }
                                    try {
                                        gameWaitWindow = new GameWaitWindow(socket, username, account, primaryStage, lobbyScene);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    gameWaitWindow.ShowWindow();
                                });
                            } else if (strSelect.equals("password")) {//提示输入密码
                                Platform.runLater(() -> {
                                    try {
                                        selectRoomWindow = new SelectRoomWindow(socket, roomId);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    selectRoomWindow.ShowWindow();
                                });

                            } else if (strSelect.equals("password error")) {//提示输入密码
                                Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "密码错误！").showAndWait());
                            }
                        }

                        /*
                        //房主成功创建房间 并直接进入房间
                        case "Create room" -> {
                            String strSelect = st.nextToken();
                            if (strSelect.equals("success")) {//进入游戏房间
                                Platform.runLater(() -> {
                                    try {
                                        gameWaitWindow=new GameWaitWindow(socket,username,account,primaryStage,lobbyScene);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    gameWaitWindow.ShowWindow();
                                });
                            }
                        }*/



                        //在进入房间后的聊天框的聊天内容
                        case "roomTalk" -> {
                            String strTalk = st.nextToken();
                            Platform.runLater(() -> gameWaitWindow.AddTxt("\n" + strTalk));
//                            Platform.runLater(() -> txtViewTalk.appendText("\n" + strTalk));
                        }
                        //在进入房间后的，更新房间里面的在线用户
                        case "room online" -> Platform.runLater(() -> {
                            gameWaitWindow.ClearTalkTo();
                            gameWaitWindow.AddTalkTo("All");
                            while (st.hasMoreTokens()) {
                                String strOnline = st.nextToken();
                                gameWaitWindow.AddTalkTo(strOnline);
                            }
                        });

                        case "lobby" -> {
                            if (primaryStage.isShowing()) {
                                Platform.runLater(() -> {
                                    container.getChildren().clear();
                                    while (st.hasMoreTokens()) {
                                        String roomNum = st.nextToken();
                                        String Id = st.nextToken();
                                        String roomName = st.nextToken();
                                        String hostName = st.nextToken();
                                        String enterNum = st.nextToken();
                                        String userNum = st.nextToken();
                                        String status = st.nextToken();
                                        HBox hBox = createHBox(roomNum, Id, roomName, hostName, enterNum, userNum, status);
                                        container.getChildren().add(hBox);
                                    }
                                });
                            }
                        }
                        //////////////////客户端接收到房主解散该房间的消息////////////////////////
                        case "Owner exitRoom" -> {
                            //获取到房间号  同当前用户进行比较 以判断是否发送消息
                            String RoomNum = st.nextToken();
                            //只有房间内的普通用户 才会收到对应的提示
                            if (!account.equals(RoomNum)) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.WARNING, "房主已解散该房间，您将返回大厅");
                                    alert.setOnCloseRequest(event -> {
                                        primaryStage.setScene(lobbyScene); // 返回到大厅场景
                                        primaryStage.show();
                                    });
                                    alert.showAndWait();
                                });
                            }
                        }

                        /////////////////////////处理是否开始游戏////////////////////////
                        case "begin game" -> {
                            //获取到是否成功
                            String judge = st.nextToken();
                            if (judge.equals("succeed")) {
//                                in.close();

//                                System.out.println("game begin!!!!!!!");
//                                String port = st.nextToken();
                                //可以开始游戏了  ///////////////// 进入游戏界面////////////////////////////////////////////
                                Platform.runLater(() -> {

                                    // TODO: 添加GamePane
                                    try {
                                        startGame();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }

                                });
                            } else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.WARNING, "还有玩家没有准备，无法开始游戏！");
                                    alert.showAndWait();
                                });
                            }
                        }
                    }

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////
    /*
     * gameStatus:
     * lobby: 在大厅
     * play: 已经开始游戏
     * wait: 进入房间
     * ready: 准备但没开始游戏
     * */
//    String gameStatus;
    Stage gameStage;
    GamePane gamePane;
    // 开始游戏
    void startGame() throws IOException {
//        System.out.println("[info] get port: "+port);
        // 设置游戏状态为Start
        gameStatusChange("start");

        // 创建对应端口号的游戏Pane
        gamePane = new GamePane(this.socket);
        Scene scene = new Scene(gamePane);

        gameStage = new Stage();
        gameStage.setTitle("坦克大战联机版");
        gameStage.setScene(scene);
        gameStage.setResizable(false);

        gameStage.showAndWait();

        // 设置游戏状态为End
        gameStatusChange("end");
    }

    void gameStatusChange(String status) throws IOException {
        switch (status) {
            case "start" -> {
//                gameStatus="play";
//                out.close();
                gameStart = true;
                Platform.runLater(() -> gameWaitWindow.AddTxt("\n" + "所有人已取消准备"));
                // 设置房间窗口状态为正在游戏
                this.gameWaitWindow.changeStatus("play");
                primaryStage.hide();
//                System.out.println("[info] clientStatus: "+gameStatus);
            }
            case "end" -> {
//                gameStatus="ready";
//                System.out.println("game end!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                this.gameStart = false;

                // 设置房间窗口状态为准备开始
                this.gameWaitWindow.changeStatus("ready");
//                gamePane.closeCamePane();
                gameStart = false;
                // 发送游戏结束信息给服务端
                primaryStage.show();
                new Thread(new ClientThread()).start();
                Communicate.send(socket, "gameOver");

//                System.out.println("[info] clientStatus: "+gameStatus);
            }
        }
    }
}
