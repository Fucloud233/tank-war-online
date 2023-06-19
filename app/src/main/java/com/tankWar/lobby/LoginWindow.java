package com.tankWar.lobby;

import com.tankWar.communication.Communicate;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class LoginWindow extends Application {
    // 连接相关的
    Socket socket = null;
    String address;
    int port;

    // 登陆界面的UI
    TextField txtAcount;
    PasswordField txtPassword;
    Button funcButton; //登录按钮
    //注册界面的UI
    TextField txtNickName;
    Button changeStatusButton;
    VBox mainVBox;
    Stage primaryStage;
    String curStatus = "login";
    VBox inputVbox;
    Label nickLbl;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 账号输入框
        Label accountLbl = new Label("账号：");
        accountLbl.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 16));
        txtAcount = new TextField();
        txtAcount.setText("1");
        txtAcount.setMinSize(210, 30);

        // 密码输入框
        Label passLbl = new Label("密码：");
        passLbl.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 16));
        txtPassword = new PasswordField();
        txtPassword.setText("111111"); ///////////固定初始值 方便测试 后续删除
        txtPassword.setMinSize(210, 30);

        // 昵称输入框
        nickLbl = new Label("昵称：");
        nickLbl.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 16));
        txtNickName = new TextField();
        txtNickName.setMinSize(210, 30);

        //功能按钮
        funcButton = new Button("登录");
        funcButton.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 14));
        funcButton.setMinSize(70, 30);
        //点击登录按钮后触发
        funcButton.setOnAction(e -> {
            funcButtonEvent();
        });

        //切换状态按钮
        changeStatusButton = new Button("注册");
        changeStatusButton.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 14));
        changeStatusButton.setMinSize(70, 30);
        //点击注册按钮后触发
        changeStatusButton.setOnAction(e -> {
            switchStatus();
        });

        /* 设置设置按钮 */
        // 参考代码: https://www.jianshu.com/p/a9df3e863c70
        Button settingButton = new Button();
        ImageView settingImg = new ImageView(new Image("/icon/setting.png"));
        settingImg.setFitWidth(20);
        settingImg.setFitHeight(20);

        // 设置颜色
        // 参考代码: https://stackoverflow.com/questions/61878752/how-to-change-the-color-of-a-png-image-in-javafx
        ColorAdjust bright = new ColorAdjust(0, 1, 1, 1);
        // 不按下时的属性
        Lighting effect1 = new Lighting(new Light.Distant(45, 90, Color.WHITE));
        effect1.setContentInput(bright);
        effect1.setSurfaceScale(0.0);
        // 按下时的属性
        Lighting effect2 = new Lighting(new Light.Distant(45, 90, Color.GREY));
        effect2.setContentInput(bright);
        effect2.setSurfaceScale(0.0);

        settingButton.setAlignment(Pos.CENTER_RIGHT);
        settingButton.setGraphic(settingImg);
        settingButton.setBackground(Background.EMPTY);
        settingButton.effectProperty().bind(
                Bindings.when(settingButton.hoverProperty())
                        .then((Effect)effect2)
                        .otherwise(effect1));
        settingButton.setOnAction((e)->{
            SettingStage settingStage = new SettingStage(this.address, this.port);
            primaryStage.close();
            settingStage.showAndWait();
            primaryStage.show();

            // 重新设置地址和端口
            this.address = settingStage.getAddress();
            this.port = settingStage.getPort();
            this.connectServer();
        });

        //加个坦克大战的标题
        Label titleLabel = new Label("坦克大战");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 30));
        titleLabel.setStyle("-fx-text-fill: #0e2a10;");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);

        // 信息输入框
        inputVbox = new VBox(accountLbl, txtAcount, passLbl, txtPassword);
        inputVbox.setAlignment(Pos.CENTER_LEFT);
        inputVbox.setSpacing(8);

        //放置登录和注册的按钮
        HBox buttonBox = new HBox(30, funcButton, changeStatusButton);
        buttonBox.setAlignment(Pos.CENTER);

//        mainVBox = new VBox(titleBox, inputVbox, buttonBox);
        mainVBox = new VBox(settingButton, titleBox, inputVbox, buttonBox);
        mainVBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #4D774E, #9C8B56, #614D79);");
        mainVBox.setSpacing(10);
        mainVBox.setPadding(new Insets(8, 20, 8, 20));
        mainVBox.setPrefSize(LobbyConfig.Width, LobbyConfig.Height);

        this.primaryStage = primaryStage;
        // 禁用窗口大小调整
        this.primaryStage.setResizable(false);
        //初始为登陆界面
        this.primaryStage.setTitle("登录窗口");
        this.primaryStage.setScene(new Scene(mainVBox));
        this.primaryStage.show();
    }

    void switchStatus() {
        switch (curStatus) {
            case "login" -> {
                curStatus = "register";
                primaryStage.setTitle("注册窗口");
                inputVbox.getChildren().add(nickLbl);
                inputVbox.getChildren().add(txtNickName);
                changeStatusButton.setText("回到登录");
                funcButton.setText("确认注册");
                primaryStage.setHeight(365);
            }
            case "register" -> {
                curStatus = "login";
                primaryStage.setTitle("登录窗口");
                inputVbox.getChildren().remove(txtNickName);
                inputVbox.getChildren().remove(nickLbl);
                changeStatusButton.setText("注册");
                funcButton.setText("登录");
                primaryStage.setHeight(310);
            }
        }
    }

    void funcButtonEvent() {
        boolean infoCorrect = false;
        //输入的信息全不为空
        if (!txtAcount.getText().isEmpty() && !txtPassword.getText().isEmpty()) {
            infoCorrect = true;

            if(socket == null) {
                connectServer();
            }

            if(!socket.isConnected()) {
                showConnectFail();
                return;
            }

            switch (curStatus) {
                case "login" -> {
                    //获取登陆的账号和密码//发送给服务器
                    String strSend = "login|" + txtAcount.getText() + "|" + txtPassword.getText();
                    Communicate.send(socket, strSend);
                    //进行登录
                    initLogin();
                }
                case "register" -> {
                    if (!txtNickName.getText().isEmpty()) {
                        //获取登陆的账号和密码//发送给服务器
                        String strSend = "register|" + txtNickName.getText() + "|" + txtAcount.getText() + "|" + txtPassword.getText();
                        Communicate.send(socket, strSend);
                        initRegister();
                    } else {
                        infoCorrect = false;
                    }
                }
            }
        }
        if (!infoCorrect) {
            new Alert(Alert.AlertType.WARNING, "请输入全部的信息").showAndWait();
        }
    }

    //登录的逻辑
    void initLogin() {
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
                funcButton.setDisable(true);
                primaryStage.close();
                //传入参数并跳转到房间选择页面 connectServer()获取到对应的信息
                Client client = new Client(nickname, txtAcount.getText(), socket);
                client.RunClient();
            }

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
    void initRegister() {
        String strReceive = Communicate.receive(socket);
        StringTokenizer st = new StringTokenizer(strReceive, "|");
        String strKey = st.nextToken();
        if (strKey.equals("register")) {
            String strStatus = st.nextToken();
            switch (strStatus) {
                case "success" -> showRegistrationSuccess();
                case "name" -> new Alert(Alert.AlertType.WARNING, "昵称已被使用！").showAndWait();
                case "account" -> new Alert(Alert.AlertType.WARNING, "账号已存在！").showAndWait();
            }
        }
    }

    // 创建注册成功提示对话框
    void showRegistrationSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("注册成功");
        alert.setHeaderText(null);
        alert.setContentText("注册成功，请返回登录界面");

        // 设置对话框的关闭请求事件处理程序
        alert.setOnCloseRequest(event -> {
            // 返回登录界面
            switchStatus();
        });

        // 显示对话框
        alert.showAndWait();
    }


    public LoginWindow(String address) {
        this.address = address;
        this.port = 8080;

        // 连接服务器
        connectServer();
    }

    // [test] 用于自动登陆
    public void login(String account) {
        // 进行登录
        String strSend = "login|" + account + "|" + "111111";
        Communicate.send(socket, strSend);
        initLogin();
}

    // 在客户端启动时 首先连接服务端
    void connectServer() {
        try {
            //创建套接字
            socket = new Socket(this.address, this.port);
        } catch (IOException e) {
            // 如果连接失败 则需要重新配置
            showConnectFail();
        }
    }

    void showConnectFail() {
        System.out.println("[error] 连接失败");
        new Alert(Alert.AlertType.WARNING, "连接服务器失败，请检查连接配置。").showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
