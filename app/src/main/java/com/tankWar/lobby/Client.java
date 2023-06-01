package com.tankWar.lobby;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class Client extends Application {

    // 连接相关的
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    //游戏大厅的UI
    private Button addbtn;
    //    private GridPane gridPane;
//    private Label[] roomNumLabel=new Label[12];
//    private Button[] roomBtn=new Button[12];
    private VBox container;
    private ScrollPane scrollPane;


    //聊天框界面的UI
    private TextField txtTalk;
    private TextArea txtViewTalk;
    private Button btnTalk;
    private ComboBox<String> listOnline;

    // 登陆界面的UI
    private TextField txtServerIP;

    private TextField txtName;
    private PasswordField txtPassword;
    private Button btnLogin;
    private Button btnRegister;
    private String userid;

    // 聊天数据
    private String strSend;
    private String strReceive;
    private String strKey;
    private StringTokenizer st;
    //登录界面
    private Stage loginStage;
    //聊天室界面
    private Stage primaryStage;
    private CreateRoomWindow roomWindow;
    private SelectRoomWindow selectRoomWindow;////////////////////////////////////////////
    private GameWaitWindow gameWaitWindow;
    //一些参数
    private String selectRoom="-1";//玩家房间选择
    private String state;//准备状态
    private boolean isInGame=false;//判断是否从游戏里出来
    private String serverMessage;//服务器消息

    @Override
    public void start(Stage primaryStage) throws IOException {
        //绑定
        this.primaryStage=primaryStage;
        primaryStage.setTitle("游戏大厅");

        BorderPane borderPane = new BorderPane();

        txtTalk = new TextField();
        txtViewTalk = new TextArea();
        btnTalk = new Button("发送");
        listOnline = new ComboBox<>();
        listOnline.getItems().add("All");

        txtViewTalk.setEditable(false);
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(10));
        hBox.getChildren().addAll(new Label("Talk:"), txtTalk, new Label("To:"), listOnline, btnTalk);
        VBox vBox=new VBox();
        vBox.getChildren().add(txtViewTalk);
        vBox.getChildren().add(hBox);
        ///////////////////////////////////
        container=new VBox();
        scrollPane=new ScrollPane(container);
        VBox.setVgrow(container,Priority.ALWAYS);

        //////////////////////////////////

//        gridPane = new GridPane();
//        gridPane.setPadding(new Insets(20, 20, 20, 20));
//        gridPane.setVgap(20);
//        gridPane.setHgap(40);
//        gridPane.setAlignment(Pos.CENTER);
//        for (int i = 0; i < 12; i++) {
//            roomBtn[i]=new Button(i+"号房间");
//            roomNumLabel[i]=new Label("已有0人");
//            roomNumLabel[i].setAlignment(Pos.CENTER);
//
///////////////////////////////////////////////////////////////////
//            roomBtn[i].setMinWidth(100); // 设置最小宽度
//            roomBtn[i].setMinHeight(50); // 设置最小高度
//            roomBtn[i].setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;"); // 设置按钮样式
//            VBox vBox1=new VBox(roomBtn[i],roomNumLabel[i]);
//           int finalI = i;
//            roomBtn[i].setOnAction(event -> {
//                this.selectRoom=String.valueOf(finalI);
//                try {
//                    selectRoomWindow=new SelectRoomWindow(socket,selectRoom,userid);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                selectRoomWindow.ShowWindow();
//
//            });
////////////////////////////////////////////////////////////////////////
//            // 设置事件 进行房间选择后的处理逻辑
//            // int finalI = i;
//            // button.setOnAction(e -> player.setSelectRoom(String.valueOf(finalI)));
//
//            gridPane.add(vBox1, i % 3, i / 3);
//
//        }
//        roomNumLabel=new Label[12];


        addbtn=new Button("创建房间");
        addbtn.setPrefHeight(50);
        addbtn.setPrefWidth(100);
        addbtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");
        VBox lobbyVB=new VBox();
        lobbyVB.setPadding(new Insets(10,10,10,40));
        lobbyVB.setAlignment(Pos.CENTER);
        lobbyVB.getChildren().add(addbtn);


        borderPane.setCenter(scrollPane);///////////////////////////
        borderPane.setBottom(vBox);
        borderPane.setLeft(lobbyVB);


        // Button action
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                out.println("talk|" + txtTalk.getText() + "|" + txtName.getText() + "|" + listOnline.getValue());
                txtTalk.clear();
            }
        });

        addbtn.setOnAction(e->{
//            try {
//                roomWindow=new CreateRoomWindow(socket);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
            try {
                roomWindow = new CreateRoomWindow(socket,txtName.getText());
            } catch (IOException ex) {
                // 处理异常，例如打印错误信息或进行其他操作
                ex.printStackTrace();
                // 或者显示错误提示给用户
                // throw new RuntimeException(ex);
            }
            roomWindow.ShowWindow();
//            strSend=roomWindow.getStrmes();


        });

        //聊天窗口的场景

        Scene scene = new Scene(borderPane, 800, 600);
        primaryStage.setScene(scene);

        // 登录的UI
        txtServerIP = new TextField("172.17.32.53");
        txtName = new TextField();
        txtName.setText("76135896");
        txtPassword = new PasswordField();
        txtPassword.setText("123456");


        //登录按钮的功能
        btnLogin = new Button("登录");
        btnLogin.setOnAction(e -> {
            //输入的信息全不为空
            if (!txtServerIP.getText().isEmpty() && !txtName.getText().isEmpty() && !txtPassword.getText().isEmpty()) {
                try {
                    //连接服务器
                    connectServer();
                    userid=txtName.getText();
                    //获取登陆的账号和密码
                    strSend = "login|" + txtName.getText() + "|" + txtPassword.getText();
                    //发送给服务器
                    out.println(strSend);
                    //进行登录
                    initLogin();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "请输入全部的信息").showAndWait();
            }
        });

        //注册按钮的功能
        btnRegister =new Button("注册");
        btnRegister.setOnAction(e->{
            if (!txtServerIP.getText().isEmpty() && !txtName.getText().isEmpty() && !txtPassword.getText().isEmpty()) {
                try {
                    connectServer();
                    strSend = "register|" + txtName.getText() + "|" + txtPassword.getText();
                    out.println(strSend);
                    initRegister();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "请输入全部的信息").showAndWait();
            }
        });

        loginStage = new Stage();
        loginStage.setTitle("登录窗口");
        //构建网格布局
        GridPane loginPane = new GridPane();
        loginPane.setVgap(10);
        loginPane.setHgap(10);
        loginPane.setPadding(new Insets(20));
        loginPane.add(new Label("Server IP:"), 0, 0);
        loginPane.add(txtServerIP, 1, 0);
        loginPane.add(new Label("账号:"), 0, 1);
        loginPane.add(txtName, 1, 1);
        loginPane.add(new Label("密码:"), 0, 2);
        loginPane.add(txtPassword, 1, 2);
        //列、行、列方向上跨度、行方向上跨度
        loginPane.add(btnLogin, 1, 3, 1, 2);
        loginPane.add(btnRegister, 2, 3, 1, 2);

        loginStage.setScene(new Scene(loginPane));
        //登录界面
        loginStage.showAndWait();
        // 登录成功后显示主聊天窗口
        primaryStage.show();

//        while (true) {
//            if (!isInGame) {
//                this.selectRoom = "-1";
////                SelectRoomWindow selectRoomWindow = new SelectRoomWindow(this.x, this.y);
////                selectRoomWindow.showSelectRoom(this);//调试选择房间窗口
//                serverMessage = in.readLine();
//                out.println(this.selectRoom);
//                refresh(serverMessage);
//                while (true) {
////                    refreshWindowLocate(selectRoomWindow.getLocate());//更新位置
//                    serverMessage = in.readLine();//服务器发过来的房间信息
//                    if (serverMessage.equals("full")) {//前端弹出提示
//                        System.out.println("房间人数已满");
//                        JOptionPane.showMessageDialog(null, "房间人数已满");
//                        this.selectRoom = "-1";
//                    }
//                    if (serverMessage.equals("ok")) {//前端在后面显示进入房间
//                        out.println("1");
//                        primaryStage.close();//关闭选择房间窗口
//                        break;
//                    }
//                    storeRoomNum(serverMessage, selectRoomWindow);//储存大厅房间状态(人数)
//                    out.println(this.selectRoom);
//
//                }
//            }
//            if (gameReady())//游戏准备环节
//                break;
//        }

    }

    public static void main(String[] args) {
        launch(args);
    }


    //连接服务器
    void connectServer() {
        try {
            System.out.println(txtServerIP.getText());
            //创建套接字的
            socket = new Socket(txtServerIP.getText(), 8888);
            //输入流和输出流
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void refresh(String s){
//        String[] info = s.split(",");
//        for (int i = 0;i < 12;i++){
//            this.roomNumLabel[i].setText("已有"+info[i]+"个玩家");
//        }
//    }

    //登录的逻辑
    private void initLogin() throws IOException {
        strReceive = in.readLine();
        //截断获取关键的信息内容
        st = new StringTokenizer(strReceive, "|");
        strKey = st.nextToken();
        System.out.println(strKey);

        if (strKey.equals("login")) {
            String strStatus = st.nextToken();
            System.out.println(strStatus);
            //如果成功登录
            if (strStatus.equals("succeed")) {
                btnLogin.setDisable(true);
                btnTalk.setDisable(false);
                new Thread(new ClientThread(socket)).start();
                out.println("init|online");
                //登陆成功之后 关闭登录窗口 显示聊天室窗口
                Platform.runLater(()->{
                    loginStage.close();
                    primaryStage.show();
                });
            }
            new Alert(Alert.AlertType.INFORMATION, strKey + " " + strStatus + "!").showAndWait();
        }
        if (strKey.equals("warning")) {
            String strStatus = st.nextToken();
            new Alert(Alert.AlertType.WARNING, strStatus).showAndWait();
        }
    }

    //注册的逻辑
    private void initRegister() throws IOException {
        strReceive = in.readLine();
        st = new StringTokenizer(strReceive, "|");
        strKey = st.nextToken();
        if (strKey.equals("register")) {
            String strStatus = st.nextToken();
            new Alert(Alert.AlertType.INFORMATION, strKey + " " + strStatus + "!").showAndWait();
        }
        if (strKey.equals("warning")) {
            String strStatus = st.nextToken();
            new Alert(Alert.AlertType.WARNING, strStatus).showAndWait();
        }
    }


    class ClientThread implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private String strReceive, strKey;
        private StringTokenizer st;

        public ClientThread(Socket s) throws IOException {
            this.socket = s;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        public HBox createHBox(String roomNum,int enterNum,int userNum) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);
            hBox.setPadding(new Insets(10));

            Label roomLabel = new Label(roomNum + "的房间");
            Label countLabel = new Label("人数：" + enterNum+"/"+userNum);
            Button enterBtn = new Button();
            if (enterNum<userNum){
                enterBtn.setText("进入");
            }else {
                enterBtn.setText("游戏中");
            }

            enterBtn.setOnAction(event -> {
                if (enterBtn.getText().equals("进入")){
                    try {
                        selectRoomWindow=new SelectRoomWindow(socket,roomNum,userid);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    selectRoomWindow.ShowWindow();
                }else{
                    Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "游戏中无法进入").showAndWait());
                }
            });

            hBox.getChildren().addAll(roomLabel, countLabel, enterBtn);
            return hBox;
        }


        public void run() {
            while (true) {
                try {
                    strReceive = in.readLine();
                    st = new StringTokenizer(strReceive, "|");
                    strKey = st.nextToken();
                    if (strKey.equals("talk")) {
                        String strTalk = st.nextToken();
                        Platform.runLater(() -> txtViewTalk.appendText("\n" + strTalk));
                    } else if (strKey.equals("online")) {
                        Platform.runLater(()->{
                            listOnline.getItems().clear();
                            listOnline.getItems().add("所有人");
                            while (st.hasMoreTokens()) {
                                String strOnline = st.nextToken();
                                listOnline.getItems().add(strOnline);

                            }
                        });

                    } else if (strKey.equals("remove")) {
                        while (st.hasMoreTokens()) {
                            String strRemove = st.nextToken();
                            Platform.runLater(() -> listOnline.getItems().remove(strRemove));
                        }
                    } else if (strKey.equals("warning")) {
                        String strWarning = st.nextToken();
                        Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, strWarning).showAndWait());
                    } else if (strKey.equals("Create")) {
//                        while (st.hasMoreTokens()){
//                            String strCreate =st.nextToken();
//                            if (strCreate.equals("Failed")){
//                                Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,"房间已被使用，请重新创建房间").showAndWait());
//                            } else if (strCreate.equals("Success")) {
//                                System.out.println("create ok!");
//                                Platform.runLater(()->roomWindow.CloseWindow());
//                            }
//                        }
                        while (st.hasMoreTokens()){
                            String strCreate =st.nextToken();
                            if (strCreate.equals("Failed")){
                                Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,"你已经创建过房间了！").showAndWait());
                            } else {
                                Platform.runLater(()->{
                                    roomWindow.CloseWindow();
                                });
//                                String roomNumber = strCreate;
//                                String roomCount = st.nextToken();
//
//                                Platform.runLater(() -> {
//                                    HBox hBox = new HBox();
//                                    hBox.setAlignment(Pos.CENTER);
//                                    hBox.setPadding(new Insets(10));
//
//                                    Label nlabel = new Label(roomNumber + "的房间");
//                                    nlabel.setId("roomLabel");
//                                    Label plabel = new Label("人数：" + roomCount);
//                                    plabel.setId("countLabel");
//                                    Button enterBtn = new Button("进入");
//                                    enterBtn.setOnAction(e->{
//                                        try {
//                                            selectRoomWindow=new SelectRoomWindow(socket,roomNumber,userid);
//                                        } catch (IOException ex) {
//                                            throw new RuntimeException(ex);
//                                        }
//                                        selectRoomWindow.ShowWindow();
//                                    });
//
//                                    hBox.getChildren().addAll(nlabel, plabel, enterBtn);
//                                    container.getChildren().add(hBox);




                            }

                        }
                    } else if (strKey.equals("select room")) {
                        String strSelect=st.nextToken();
//                        if (strSelect.equals("no created")){
//                            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,"该房间没有被创建！").showAndWait());
//                        }
//                        if (strSelect.equals("userNum error")) {
//                            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,"该房间人数达到上限！").showAndWait());
//                        }
                        if (strSelect.equals("password error")) {
                            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,"密码错误！").showAndWait());
                        } else if (strSelect.equals("success")){
                            System.out.println("sss");
                            Platform.runLater(() ->{
                                selectRoomWindow.CloseWindow();
                                primaryStage.close();
//                                GameWaitWindow gameWaitWindow= null;
                                try {
                                    gameWaitWindow = new GameWaitWindow(socket,txtName.getText());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                gameWaitWindow.setOnClosedCallback(() -> {
                                    // 在回调函数中恢复上一个页面的内容或执行其他操作
                                    primaryStage.show();
                                });
                                primaryStage.hide();
                                gameWaitWindow.ShowWindow();
                            });
                        }
                    } else if (strKey.equals("lobby")) {
                        if (primaryStage.isShowing()){
                            Platform.runLater(()->{
                                container.getChildren().clear();
                                while (st.hasMoreTokens()){
                                    String roomNum=st.nextToken();
                                    int enterNum= Integer.parseInt(st.nextToken());
                                    int userNum= Integer.parseInt(st.nextToken());
                                    HBox hBox=createHBox(roomNum,enterNum,userNum);
                                    container.getChildren().add(hBox);
                                }
                            });
                        }

                    } else if (strKey.equals("roomTalk")) {
                        String strTalk = st.nextToken();
                        Platform.runLater(()->gameWaitWindow.AddTxt("\n" + strTalk));
//                        Platform.runLater(() -> txtViewTalk.appendText("\n" + strTalk));
                    }else if (strKey.equals("room online")) {
                        Platform.runLater(()->{
                            gameWaitWindow.ClearTalkTo();
                            gameWaitWindow.AddTalkTo("所有人");
                            while (st.hasMoreTokens()) {
                                String strOnline = st.nextToken();
                                gameWaitWindow.AddTalkTo(strOnline);

                            }
                        });

                    }
                    Thread.sleep(1000);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}