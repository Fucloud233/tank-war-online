package com.tankWar.lobby;


import com.tankWar.communication.Communicate;
import com.tankWar.game.component.GamePane;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
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

    // 用来记录服务单发送的房间内的所有玩家名称
    // 游戏开始时则发送给GamePane以显示计分板
    String[] playerNames = null;
    final static int MaxPlayerNum = 4;

    boolean gameStart = false;
    ///表格///
    private TableView<RoomItem> tableView;
    private ObservableList<RoomItem> roomList;


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
        listOnline.setValue("All"); // 设置"ALL"为默认选项
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
        VBox container = new VBox();
        //设置房间背景颜色
        container.setStyle("-fx-background-color: #494f3c;-fx-control-inner-background: #494f3c");

        container.setMinHeight(380); // 设置容器的固定高度

        // 创建表格视图和数据列表
        tableView = new TableView<>();
        roomList = FXCollections.observableArrayList();

        // 创建表格列
        TableColumn<RoomItem, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<RoomItem, String> nameColumn = new TableColumn<>("房间名");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<RoomItem, String> accountColumn = new TableColumn<>("房间号");
        accountColumn.setCellValueFactory(new PropertyValueFactory<>("account"));

        TableColumn<RoomItem, String> ownerColumn = new TableColumn<>("房主");
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));

        TableColumn<RoomItem, Integer> playersColumn = new TableColumn<>("房间人数");
        playersColumn.setCellValueFactory(new PropertyValueFactory<>("enterNum"));

        TableColumn<RoomItem, Integer> limitColumn = new TableColumn<>("人数上限");
        limitColumn.setCellValueFactory(new PropertyValueFactory<>("userNum"));

        TableColumn<RoomItem, String> statusColumn = new TableColumn<>("房间状态");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 设置列宽
        idColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));
        nameColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));
        accountColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));
        ownerColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));
        playersColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));
        limitColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));
        statusColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));

        // 将列添加到表格视图中
        tableView.getColumns().addAll(idColumn, nameColumn, accountColumn,ownerColumn, playersColumn, limitColumn, statusColumn);

        //绑定数据
        tableView.setItems(roomList);


        // 设置表格的选择模式为单选
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // 监听表格选中项的变化
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                RoomItem selectedRoom = tableView.getSelectionModel().getSelectedItem();
                // 获取选中行的某一字段的信息
                roomId = selectedRoom.getAccount();
                isHBoxSelected=true;
                System.out.println("Selected room account: " + roomId);
            }
        });
        //把表格加入vbox
        container.getChildren().add(tableView);


        //创建房间按钮
        //游戏大厅的UI
        Button newRoomBtn = new Button("创建房间");
        //把原先的字体设置删除掉了
        newRoomBtn.setStyle(" -fx-base: #b6e7c9;");

        //加入房间按钮
        Button enterRoomBtn = new Button("加入房间");
        enterRoomBtn.setStyle(" -fx-base: #b6e7c9;");

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
        borderPane.setCenter(container);
        borderPane.setBottom(BottomBox);
        //设置边距
        borderPane.setPadding(new Insets(5, 10, 10, 10));

        lobbyScene = new Scene(borderPane, 800, 700);
        //设置像素字体，直接在scene上设置
        URL styleURL = this.getClass().getResource("/css/label.css");
        if(styleURL != null)
            lobbyScene.getStylesheets().add(styleURL.toExternalForm());

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
            if (isHBoxSelected){
                System.out.println("roomid:"+roomId);
                //向服务端传选择的房间内容
                Communicate.send(socket, "Select room|" + roomId);
                isHBoxSelected=false;
            } else {
                new Alert(Alert.AlertType.WARNING, "请选择房间！").showAndWait();
            }
        });

        // 聊天框中的按钮事件
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                //获取用户输入的账号
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
                isHBoxSelected=false;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            //显示页面
            roomWindow.ShowWindow();
        });
    }

    class ClientThread implements Runnable {
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
                            listOnline.setValue("All"); // 设置"ALL"为默认选项
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
                            //先清空 然后重新获取后重新渲染
                            gameWaitWindow.ClearTalkTo();
                            gameWaitWindow.AddTalkTo("All");

                            // 读取首字段 (用户的数量)
                            int size = Integer.parseInt(st.nextToken());
                            // 根据实际玩家数初始化
                            playerNames = new String[size];
                            for(int i=0; i<size && st.hasMoreTokens(); i++) {
                                // 通过*号来截取用户的信息
                                String strOnline = st.nextToken();
                                String[] parts = strOnline.split("\\*");

                                // 获取昵称 和 状态
                                String name = parts[0], status = parts[1];

                                // 将玩家名按顺序添加
                                gameWaitWindow.AddTalkTo(name);
                                playerNames[i] = name;

                                // 设置所有玩家的信息
                                gameWaitWindow.newAddTalkTo(i, name, i==0 ? "房主" : status);

                            }
                        });

                        case "lobby" -> {
                            if (primaryStage.isShowing()) {
                                Platform.runLater(() -> {
                                    roomList.clear();
                                    while (st.hasMoreTokens()) {
                                        String roomNum = st.nextToken();
                                        String Id = st.nextToken();
                                        String roomName = st.nextToken();
                                        String hostName = st.nextToken();
                                        String enterNum = st.nextToken();
                                        String userNum = st.nextToken();
                                        String status = st.nextToken();
                                        System.out.println('&'+roomNum+' '+Id+' '+roomName+' '+hostName+' '+enterNum+' '+userNum+' '+status);
                                        RoomItem roomItem = new RoomItem(Integer.parseInt(Id),roomName,roomNum,hostName,Integer.parseInt(enterNum),Integer.parseInt(userNum),status);
                                        roomList.add(roomItem);
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
                            String judge = st.nextToken();
                            // 获取到是否成功
                            if (judge.equals("succeed")) {
                                Platform.runLater(this::startGame);
                                break;
                            }

                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.WARNING, "还有玩家没有准备，无法开始游戏！");
                                alert.showAndWait();
                            });

                        }
                    }

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // 开始游戏
        void startGame() {
//        System.out.println("[info] get port: "+port);
            // 设置游戏状态为Start
            gameStatusChange("start");

            // 创建对应端口号的游戏Pane
            GamePane gamePane = new GamePane(socket, playerNames);
            Scene scene = new Scene(gamePane);


            Stage gameStage = new Stage();
            gameStage.setTitle("坦克大战联机版");
            gameStage.setScene(scene);
            gameStage.setResizable(false);

            gameStage.showAndWait();

            // 设置游戏状态为End
            gameStatusChange("end");
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


    void gameStatusChange(String status) {
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
    public static class RoomItem{
        private IntegerProperty id;
        private StringProperty name;
        private StringProperty account;
        private StringProperty owner;
        private IntegerProperty enterNum;
        private IntegerProperty userNum;
        private StringProperty status;

        public RoomItem(int id, String name, String account,String owner, int players, int limit, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.account=new SimpleStringProperty(account);
            this.owner = new SimpleStringProperty(owner);
            this.enterNum = new SimpleIntegerProperty(players);
            this.userNum = new SimpleIntegerProperty(limit);
            this.status = new SimpleStringProperty(status);
        }

        public int getId() {
            return id.get();
        }
        public void setId(int id){
            this.id.set(id);
        }
        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }
        public String getOwner() {
            return owner.get();
        }

        public void setOwner(String owner) {
            this.owner.set(owner);
        }
        public int getEnterNum() {
            return enterNum.get();
        }

        public void setEnterNum(int players) {
            this.enterNum.set(players);
        }

        public int getUserNum() {
            return userNum.get();
        }

        public void setUserNum(int limit) {
            this.userNum.set(limit);
        }
        public String getStatus(){
            return status.get();
        }

        public String getAccount(){
            return account.get();
        }
        public void setAccount(String account){
            this.account.set(account);
        }
    }
}


