package com.tankWar.lobby;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class LoginWindow extends Application {
    // 连接相关的
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    String IP;
    // 登陆界面的UI
    private TextField txtName;
    private PasswordField txtPassword;
    private Button btnLogin; //登录按钮
    private GridPane loginPane;
    private Scene loginScene;
    //注册界面的UI
    private TextField txtNickName;
    private TextField txtAccount;
    private PasswordField setPassword;
    private Button btnRegister;
    private Button btnAck;
    private GridPane registerPane;
    private Scene registerScene;
    //
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        // 初始化登录的UI
        txtName = new TextField();
        txtName.setText("1");
        txtPassword = new PasswordField();
        txtPassword.setText("111111"); ///////////固定初始值 方便测试 后续删除
        //初始化注册的UI
        txtNickName = new TextField();
        txtAccount = new TextField();
        setPassword = new PasswordField();
        btnAck = new Button("确认注册");


        //登录按钮
        btnLogin = new Button("登录");
        //点击登录按钮后触发
        btnLogin.setOnAction(e -> {
            //输入的信息全不为空
            if (!txtName.getText().isEmpty() && !txtPassword.getText().isEmpty()) {
                try {
                    //连接服务器
                    connectServer();
                    //获取登陆的账号和密码//发送给服务器
                    String strSend = "login|" + txtName.getText() + "|" + txtPassword.getText();
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

        //注册按钮
        //注册按钮
        btnRegister = new Button("注册");
        //点击注册按钮后触发
        btnRegister.setOnAction(e -> {
            primaryStage.setTitle("注册窗口");
            primaryStage.setScene(registerScene);
            primaryStage.show();
        });

        //点击确认注册按钮后触发,删除了注册后就登陆的代码
        btnAck.setOnAction(event -> {
            if (!txtNickName.getText().isEmpty() && !txtAccount.getText().isEmpty() && !setPassword.getText().isEmpty()) {
                try {
                    //连接服务器
                    connectServer();
                    //获取登陆的账号和密码//发送给服务器
                    String strSend = "register|" + txtNickName.getText() + "|" + txtAccount.getText() + "|" + setPassword.getText();
                    out.println(strSend);
                    initRegister();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "请输入全部的信息").showAndWait();
            }
        });

        //登陆界面的网格布局
        loginPane = new GridPane();
        loginPane.setVgap(10);
        loginPane.setHgap(10);
        loginPane.setPadding(new Insets(20));
        loginPane.add(new Label("账号:"), 0, 1);
        loginPane.add(txtName, 1, 1);
        loginPane.add(new Label("密码:"), 0, 2);
        loginPane.add(txtPassword, 1, 2);
        //放置登录和注册的按钮
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(btnLogin, btnRegister);
        loginPane.add(buttonBox, 0, 3, 2, 1);
        loginScene = new Scene(loginPane);

        //注册界面的网格布局

        registerPane = new GridPane();
        registerPane.setVgap(10);
        registerPane.setHgap(10);
        registerPane.setPadding(new Insets(20));
        registerPane.add(new Label("昵称:"), 0, 1);
        registerPane.add(txtNickName, 1, 1);
        registerPane.add(new Label("账号:"), 0, 2);
        registerPane.add(txtAccount, 1, 2);
        registerPane.add(new Label("密码:"), 0, 3);
        registerPane.add(setPassword, 1, 3);
        HBox buttonBox2 = new HBox(10);
        buttonBox2.setAlignment(Pos.CENTER);
        buttonBox2.getChildren().addAll(btnAck);
        registerPane.add(buttonBox2, 0, 4, 2, 1);
        registerScene = new Scene(registerPane);


        // 禁用窗口大小调整
        primaryStage.setResizable(false);
        //初始为登陆界面
        primaryStage.setTitle("登录窗口");
        primaryStage.setScene(loginScene);
        primaryStage.show();


    }


    //登录的逻辑
    private void initLogin() throws IOException {
        String strReceive = in.readLine();
        //截断获取关键的信息内容
        StringTokenizer st = new StringTokenizer(strReceive, "|");
        String strKey = st.nextToken();
        System.out.println("[info] message type" + strKey);

        if (strKey.equals("login")) {
            String strStatus = st.nextToken();
            System.out.println("[info] login Status" + strStatus);
            //如果成功登录
            if (strStatus.equals("succeed")) {
                String nickname = st.nextToken();
                //转到窗口页面 并关闭登录窗口
                btnLogin.setDisable(true);
                primaryStage.close();
                //传入参数并跳转到房间选择页面 connectServer()获取到对应的信息
                Client client = new Client(nickname, txtName.getText(), socket, in, out);
                client.RunClient();
            }
//            new Alert(Alert.AlertType.INFORMATION, strKey + " " + strStatus + "!");
        }
        if (strKey.equals("warning")) {
            String strStatus = st.nextToken();
            if (strStatus.equals("double")) {
                new Alert(Alert.AlertType.WARNING, "不能重复登录！").showAndWait();
            } else {
                new Alert(Alert.AlertType.WARNING, strStatus).showAndWait();
            }

        }
    }

    //注册的逻辑
    private void initRegister() throws IOException {
        String strReceive = in.readLine();
        StringTokenizer st = new StringTokenizer(strReceive, "|");
        String strKey = st.nextToken();
        if (strKey.equals("register")) {
            String strStatus = st.nextToken();
            if (strStatus.equals("success")) {
                showRegistrationSuccess();
            } else if (strStatus.equals("name")) {
                new Alert(Alert.AlertType.WARNING, "昵称已被使用！").showAndWait();
            } else if (strStatus.equals("account")) {
                new Alert(Alert.AlertType.WARNING, "账号已存在！").showAndWait();
            }
        }
    }

    // 创建注册成功提示对话框
    private void showRegistrationSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("注册成功");
        alert.setHeaderText(null);
        alert.setContentText("注册成功，请返回登录界面");

        // 设置对话框的关闭请求事件处理程序
        alert.setOnCloseRequest(event -> {
            // 返回登录界面
            primaryStage.setTitle("登录窗口");
            primaryStage.setScene(loginScene);
            primaryStage.show();
        });

        // 显示对话框
        alert.showAndWait();
    }

    public LoginWindow(String IP) {
        this.IP = IP;
    }

    //连接服务器
    void connectServer() throws IOException {
//        String IP = ChatServer.getServerIP();
        //创建套接字
        socket = new Socket(this.IP, 8888);
        //输入流和输出流
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
