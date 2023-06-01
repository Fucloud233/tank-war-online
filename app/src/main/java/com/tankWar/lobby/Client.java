package com.tankWar.lobby;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


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

    private VBox container;
    private ScrollPane scrollPane;

    //聊天框界面的UI
    private TextField txtTalk;
    private TextArea txtViewTalk;
    private Button btnTalk;
    private ComboBox<String> listOnline;

    // 登陆界面的UI
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
    private SelectRoomWindow selectRoomWindow;
    private GameWaitWindow gameWaitWindow;
    //一些参数


    @Override
    public void start(Stage primaryStage) throws IOException {
        //绑定
        this.primaryStage=primaryStage;
        primaryStage.setTitle("游戏大厅");

        BorderPane borderPane = new BorderPane();
        //游戏大厅的聊天框部分
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

        //游戏大厅的聊天框上面右边部分
        container=new VBox();
        scrollPane=new ScrollPane(container);
        VBox.setVgrow(container,Priority.ALWAYS);

        //游戏大厅的聊天框上面左边部分
        addbtn=new Button("创建房间");
        addbtn.setPrefHeight(50);
        addbtn.setPrefWidth(100);
        addbtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");
        VBox lobbyVB=new VBox();
        lobbyVB.setPadding(new Insets(10,10,10,40));
        lobbyVB.setAlignment(Pos.CENTER);
        lobbyVB.getChildren().add(addbtn);

        //最后将组件排布在borderPane上
        borderPane.setCenter(scrollPane);
        borderPane.setBottom(vBox);
        borderPane.setLeft(lobbyVB);
        Scene scene = new Scene(borderPane, 800, 600);
        primaryStage.setScene(scene);


        // 聊天框中的按钮事件
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                out.println("talk|" + txtTalk.getText() + "|" + txtName.getText() + "|" + listOnline.getValue());
                txtTalk.clear();
            }
        });

        //创建房间按钮的事件
        addbtn.setOnAction(e->{
            try {
                roomWindow = new CreateRoomWindow(socket,txtName.getText());
            } catch (IOException ex) {
                // 处理异常，例如打印错误信息或进行其他操作
                ex.printStackTrace();
                // 或者显示错误提示给用户
                // throw new RuntimeException(ex);
            }
            roomWindow.ShowWindow();
        });

        // 登录的UI
        txtName = new TextField();
        txtName.setText("76135896");
        txtPassword = new PasswordField();
        txtPassword.setText("123456");


        //登录按钮的功能
        btnLogin = new Button("登录");
        btnLogin.setOnAction(e -> {
            //输入的信息全不为空
            if (!txtName.getText().isEmpty() && !txtPassword.getText().isEmpty()) {
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
            if (!txtName.getText().isEmpty() && !txtPassword.getText().isEmpty()) {
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
        //登陆界面的网格布局
        GridPane loginPane = new GridPane();
        loginPane.setVgap(10);
        loginPane.setHgap(10);
        loginPane.setPadding(new Insets(20));


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
    }

    public static void main(String[] args) {
        launch(args);
    }


    //连接服务器
    void connectServer() {
        try {
            String IP=ChatServer.getServerIP();
            //创建套接字
            socket = new Socket(IP, 8888);
            //输入流和输出流
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



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
        //这用来创建每一条房间的消息，显示在聊天框上面的右边部分
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
                    if (strKey.equals("talk")) {//游戏大厅部分聊天框的聊天内容
                        String strTalk = st.nextToken();
                        Platform.runLater(() -> txtViewTalk.appendText("\n" + strTalk));
                    } else if (strKey.equals("online")) {//游戏大厅部分聊天框中在线用户的更新
                        Platform.runLater(()->{
                            listOnline.getItems().clear();
                            listOnline.getItems().add("All");
                            while (st.hasMoreTokens()) {
                                String strOnline = st.nextToken();
                                listOnline.getItems().add(strOnline);

                            }
                        });

                    }  else if (strKey.equals("warning")) {//登陆失败提示
                        String strWarning = st.nextToken();
                        Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, strWarning).showAndWait());
                    } else if (strKey.equals("Create")) {//创建房间的信息
                        while (st.hasMoreTokens()){
                            String strCreate =st.nextToken();
                            if (strCreate.equals("Failed")){
                                Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,"你已经创建过房间了！").showAndWait());
                            } else {
                                Platform.runLater(()->{
                                    roomWindow.CloseWindow();
                                });

                            }

                        }
                    } else if (strKey.equals("select room")) {//选择加入房间
                        String strSelect=st.nextToken();

                        if (strSelect.equals("password error")) {
                            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,"密码错误！").showAndWait());
                        } else if (strSelect.equals("success")){

                            Platform.runLater(() ->{
                                selectRoomWindow.CloseWindow();
                                primaryStage.close();
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
                    } else if (strKey.equals("lobby")) {//更新聊天框上面右边的房间动态信息
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
                    } else if (strKey.equals("roomTalk")) {//在进入房间后的聊天框的聊天内容
                        String strTalk = st.nextToken();
                        Platform.runLater(()->gameWaitWindow.AddTxt("\n" + strTalk));
//                        Platform.runLater(() -> txtViewTalk.appendText("\n" + strTalk));
                    }else if (strKey.equals("room online")) {//在进入房间后的，更新房间里面的在线用户
                        Platform.runLater(()->{
                            gameWaitWindow.ClearTalkTo();
                            gameWaitWindow.AddTalkTo("All");
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