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
    // 登陆界面的UI
    private TextField txtName;
    private PasswordField txtPassword;
    private Button btnLogin; //登录按钮
    //
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage=primaryStage;
        // 登录的UI
        txtName = new TextField();
        txtName.setText("76135896");
        txtPassword = new PasswordField();
        txtPassword.setText("123456"); ///////////固定初始值 方便测试 后续删除
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
        Button btnRegister = new Button("注册");
        //点击注册按钮后触发
        btnRegister.setOnAction(e->{
            if (!txtName.getText().isEmpty() && !txtPassword.getText().isEmpty()) {
                try {
                    //连接服务器
                    connectServer();
                    //获取注册的账号和密码//发送给服务器
                    String strSend = "register|" + txtName.getText() + "|" + txtPassword.getText();
                    out.println(strSend);
                    //进行注册
                    initRegister();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "请输入全部的信息").showAndWait();
            }
        });

        //登陆界面的网格布局
        GridPane loginPane = new GridPane();
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
        loginPane.add(buttonBox, 1, 3);
        primaryStage.setTitle("登录窗口");
        primaryStage.setScene(new Scene(loginPane));
        primaryStage.setResizable(false); // 禁用窗口大小调整
        primaryStage.show();
    }


    //登录的逻辑
    private void initLogin() throws IOException {
        String strReceive = in.readLine();
        //截断获取关键的信息内容
        StringTokenizer st = new StringTokenizer(strReceive, "|");
        String strKey = st.nextToken();
        System.out.println(strKey);

        if (strKey.equals("login")) {
            String strStatus = st.nextToken();
            System.out.println(strStatus);
            //如果成功登录
            if (strStatus.equals("succeed")) {
                //转到窗口页面 并关闭登录窗口
                btnLogin.setDisable(true);
                primaryStage.close();

                //向服务端发送信息表示登入这个房间
                out.println("select room|"+txtName.getText()+"|"+socket+"|"+txtName.getText());

                //传入参数并跳转到房间选择页面 connectServer()获取到对应的信息
                String testName=txtName.getText();
                Client client =new Client(txtName.getText(),testName,socket,in,out);
                client.RunClient();
            }
            new Alert(Alert.AlertType.INFORMATION, strKey + " " + strStatus + "!");
        }
        if (strKey.equals("warning")) {
            String strStatus = st.nextToken();
            new Alert(Alert.AlertType.WARNING, strStatus).showAndWait();
        }
    }

    //注册的逻辑
    private void initRegister() throws IOException {
        String strReceive = in.readLine();
        StringTokenizer st = new StringTokenizer(strReceive, "|");
        String strKey = st.nextToken();
        if (strKey.equals("register")) {
            String strStatus = st.nextToken();
            new Alert(Alert.AlertType.INFORMATION, strKey + " " + strStatus + "!").showAndWait();
        }
        if (strKey.equals("warning")) {
            String strStatus = st.nextToken();
            new Alert(Alert.AlertType.WARNING, strStatus).showAndWait();
        }
    }

    //连接服务器
    void connectServer() throws IOException {
        String IP=ChatServer.getServerIP();
        //创建套接字
        socket = new Socket(IP, 8888);
        //输入流和输出流
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
