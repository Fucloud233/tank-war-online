package com.tankWar.lobby;

import com.tankWar.communication.Communicate;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class LoginWindow extends Application {
    // 连接相关的
    Socket socket = null;
    DataInputStream in = null;
    DataOutputStream out = null;
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
        btnAck.setFont(Font.font("Cursive", FontWeight.NORMAL, 14));


        //登录按钮
        btnLogin = new Button("登录");
        btnLogin.setFont(Font.font("Cursive", FontWeight.NORMAL, 14));
        //点击登录按钮后触发
        btnLogin.setOnAction(e -> {
            //输入的信息全不为空
            if (!txtName.getText().isEmpty() && !txtPassword.getText().isEmpty()) {
                try {
                    //连接服务器
                    connectServer();
                    //获取登陆的账号和密码//发送给服务器
                    String strSend = "login|" + txtName.getText() + "|" + txtPassword.getText();
                    Communicate.send(socket, strSend);
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
        btnRegister.setFont(Font.font("Cursive", FontWeight.NORMAL, 14));
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
                    Communicate.send(socket, strSend);
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
        loginPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #4D774E, #9C8B56, #614D79);");
        loginPane.setVgap(10);
        loginPane.setHgap(10);
        loginPane.setPrefSize(300,200);
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setPadding(new Insets(20));
        Label accountLbl=new Label("账号：");
        accountLbl.setFont(Font.font("Cursive", FontWeight.NORMAL, 16));
        loginPane.add(accountLbl, 0, 2);
        loginPane.add(txtName, 1, 2);
        Label passLbl=new Label("密码：");
        passLbl.setFont(Font.font("Cursive", FontWeight.NORMAL, 16));
        loginPane.add(passLbl, 0, 3);
        loginPane.add(txtPassword, 1, 3);
        //加个坦克大战的标题
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("坦克大战");
        titleLabel.setAlignment(Pos.CENTER);
        titleBox.getChildren().add(titleLabel);
        titleLabel.setFont(Font.font("Bungee", FontWeight.BOLD,30));
        titleLabel.setStyle("-fx-text-fill: #0e2a10;");

        //放置登录和注册的按钮
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(btnLogin, btnRegister);

        loginPane.add(buttonBox, 0, 4,2,1);
        loginPane.add(titleBox,0,1,2,1);
        //设置里面组件的大小
        txtName.setMinSize(200, 30);
        txtName.setPrefSize(200, 30);

        txtPassword.setMinSize(200, 30);
        txtPassword.setPrefSize(200, 30);

        btnLogin.setMinSize(100, 30);
        btnLogin.setPrefSize(100, 30);

        btnRegister.setMinSize(100, 30);
        btnRegister.setPrefSize(100, 30);

        loginScene=new Scene(loginPane);

        //注册界面的网格布局

        registerPane = new GridPane();
        registerPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #4D774E, #9C8B56, #614D79);");
        registerPane.setVgap(10);
        registerPane.setHgap(10);
        registerPane.setPadding(new Insets(20));
        Label nickLbl = new Label("昵称：");
        nickLbl.setFont(Font.font("Cursive", FontWeight.NORMAL, 16));
        registerPane.add(nickLbl, 0, 2);
        registerPane.add(txtNickName, 1, 2);
        Label accLbl = new Label("账号：");
        accLbl.setFont(Font.font("Cursive", FontWeight.NORMAL, 16));
        registerPane.add(accLbl, 0, 3);
        registerPane.add(txtAccount, 1, 3);
        Label passwordLbl = new Label("密码：");
        passwordLbl.setFont(Font.font("Cursive", FontWeight.NORMAL, 16));
        registerPane.add(passwordLbl, 0, 4);
        registerPane.add(setPassword, 1, 4);
        registerPane.setPrefSize(300,200);
        registerPane.setAlignment(Pos.CENTER);
        HBox buttonBox2 = new HBox(10);
        buttonBox2.setAlignment(Pos.CENTER);
        buttonBox2.getChildren().addAll(btnAck);
        registerPane.add(buttonBox2, 0,5,2,1);
        //加个坦克大战的标题
        HBox titleBox_1 = new HBox(10);
        titleBox_1.setAlignment(Pos.CENTER);
        Label titleLabel_1 = new Label("坦克大战");
        titleLabel_1.setAlignment(Pos.CENTER);
        titleBox_1.getChildren().add(titleLabel_1);
        titleLabel_1.setFont(Font.font("Bungee", FontWeight.BOLD,30));
        titleLabel_1.setStyle("-fx-text-fill: #0e2a10;");
        registerPane.add(titleBox_1,0,1,2,1);

        //设置注册里面的组件大小
        txtNickName.setMinSize(200, 30);
        txtNickName.setPrefSize(200, 30);
        txtAccount.setMinSize(200, 30);
        txtAccount.setPrefSize(200, 30);
        setPassword.setMinSize(200, 30);
        setPassword.setPrefSize(200, 30);
        btnAck.setMinSize(100, 30);
        btnAck.setPrefSize(100, 30);
        registerScene=new Scene(registerPane);


        // 禁用窗口大小调整
        primaryStage.setResizable(false);
        //初始为登陆界面
        primaryStage.setTitle("登录窗口");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }


    //登录的逻辑
    private void initLogin() throws IOException {
        String strReceive = Communicate.receive(socket);
//        String strReceive = in.readUTF();
        System.out.println(strReceive);
        //截断获取关键的信息内容
        StringTokenizer st = new StringTokenizer(strReceive, "|");
        String strKey = st.nextToken();
        System.out.println("[info] message type " + strKey);

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
        String strReceive = Communicate.receive(socket);
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
        socket = new Socket(this.IP, 8080);
        //输入流和输出流
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
