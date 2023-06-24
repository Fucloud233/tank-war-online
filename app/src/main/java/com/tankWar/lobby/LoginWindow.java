package com.tankWar.lobby;

import com.tankWar.communication.Communicate;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
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

import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class LoginWindow extends Application {
    // 连接相关的
    Socket socket = null;
    String address;
    int port;

    // 当前状态 (使用boolean记录)
    boolean isLogin = true;

    // 登陆界面的UI
    Stage primaryStage;
    TextField txtAcount;
    PasswordField txtPassword;
    Button funcButton; //登录按钮
    //注册界面的UI
    TextField txtNickName;
    Button changeStatusButton;
    VBox mainVBox;
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
        funcButton.setOnAction(e -> funcButtonEvent() );

        //切换状态按钮
        changeStatusButton = new Button("注册");
        changeStatusButton.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 14));
        changeStatusButton.setMinSize(70, 30);
        //点击注册按钮后触发
        changeStatusButton.setOnAction(e -> switchStatus() );

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
        mainVBox = new VBox(titleBox, inputVbox, buttonBox);
        mainVBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #4D774E, #9C8B56, #614D79);");
        mainVBox.setSpacing(10);
        mainVBox.setPadding(new Insets(8, 20, 8, 20));

        this.primaryStage = primaryStage;
        // 禁用窗口大小调整
        this.primaryStage.setWidth(LobbyConfig.LoginStageWidth);
        this.primaryStage.setHeight(LobbyConfig.LoginStageHeight);
        this.primaryStage.setResizable(false);
        // 初始为登陆界面
        this.primaryStage.setTitle("登录窗口");
        this.primaryStage.setScene(new Scene(mainVBox));
        this.primaryStage.show();

        // 读取连接信息
        LoginError error = readConfig(LobbyConfig.ConfigPath);
        if(!error.checkSuccess()) {
            error.show();
            primaryStage.close();
        }

        // 初始化连接
        initConnect();
    }

    // 不再使用设置页面 仅保留
    void setSettingButton() {
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
        });
    }

    void switchStatus() {
        if(isLogin) {
            primaryStage.setTitle("注册窗口");
            inputVbox.getChildren().add(nickLbl);
            inputVbox.getChildren().add(txtNickName);
            changeStatusButton.setText("回到登录");
            funcButton.setText("确认注册");
            this.primaryStage.setHeight(LobbyConfig.LoginStageHeight + 50);
        } else {
            primaryStage.setTitle("登录窗口");
            inputVbox.getChildren().remove(txtNickName);
            inputVbox.getChildren().remove(nickLbl);
            changeStatusButton.setText("注册");
            funcButton.setText("登录");
            this.primaryStage.setHeight(LobbyConfig.LoginStageHeight);
        }

        // 反转状态
        isLogin = !isLogin;
    }

    // 确认按钮时间
    void funcButtonEvent() {
        // 账号和密码不能为空 || 当登陆时 昵称不能为空
        if (txtAcount.getText().isEmpty() || txtPassword.getText().isEmpty() ||
                (!isLogin && txtNickName.getText().isEmpty() )) {
            new Alert(Alert.AlertType.WARNING, "请输入全部的信息").showAndWait();
            return;
        }

        // 检验是否连接
        if(socket==null || !socket.isConnected()) {
            System.out.println("[debug] 未连接上服务器");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("连接错误");
            alert.setContentText("请在连接设置中重新连接");
            alert.showAndWait();
            primaryStage.close();
            return;
        }

        if(isLogin) {
            //获取登陆的账号和密码//发送给服务器
            String strSend = "login|" + txtAcount.getText() + "|" + txtPassword.getText();
            Communicate.send(socket, strSend);
            //进行登录
            initLogin();
        } else {
            //获取登陆的账号和密码//发送给服务器
            String strSend = "register|" + txtNickName.getText() + "|" + txtAcount.getText() + "|" + txtPassword.getText();
            Communicate.send(socket, strSend);
            initRegister();
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

    // 弃用
    public LoginWindow(String address) {

    }

    public LoginWindow() {

    }

    // [test] 用于自动登陆
    public void login(String account) {
        // 进行登录
        String strSend = "login|" + account + "|" + "111111";
        Communicate.send(socket, strSend);
        initLogin();
    }

    // 读取配置文件
    LoginError readConfig(String path) {
        // 读取配置文件
        Ini ini;
        try {
            ini = new Ini(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            return LoginError.ConfigFileNotFound;
        }

        // 读取连接配置信息
        Profile.Section serverSection = ini.get("server");
        if(serverSection == null) {
            return LoginError.ConnectInfoNotFound;
        }

        // 检验关键字段是否存在
        String ip = serverSection.get("ip"),
                portText = serverSection.get("port");
        if(ip == null && portText ==null) {
            return LoginError.ConnectInfoNotFound;
        }

        // 检验配置信息是否符合格式
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            return LoginError.ConnectInfoParseError;
        }

        // 保存读取到的信息
        this.port = port;
        this.address = ip;
        return LoginError.Success;
    }

    // 建立初始化连接
    void initConnect() {
        // 开启连接线程
        new Thread(new ConnectTask()).start();
    }

    // 使用多线程连接 防止页面卡死
    class ConnectTask extends Task<Void> {
        // 连接中弹窗 (使用静态对象 保证重新连接后仍然操控的是相同的弹窗)
        static ConnectingAlert alert = new ConnectingAlert();

        public ConnectTask() {
            alert.setOnCloseRequest(e-> {
                alert.close();
                if(alert.isExit())
                    primaryStage.close();
            });

            alert.show();

            // 当连接失败时
            this.setOnFailed(e->{
                alert.close();
//                System.out.println("[debug] Server connect fail");

                // 提示连接失败
                ConnectFailAlert connectFailAlert = new ConnectFailAlert();
                connectFailAlert.showAndWait();

                boolean isReconnect = connectFailAlert.isReconnect();
                // 用户不选择重新连接 退出客户端
                if(!isReconnect) {
                    primaryStage.close();
                    return;
                }

                // 用户选择重新连接后 重新连接
                alert.reshow();
                new Thread(new ConnectTask()).start();
            });
        }

        @Override
        protected Void call() throws Exception {
            socket = new Socket(address, port);
            // 连接成功后关闭窗口
            Platform.runLater(()->alert.close());
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// 用来记录登陆时的错误
enum LoginError {
    Success("成功"),
    ConfigFileNotFound("找不到配置文件"),
    ConnectInfoNotFound("找不到连接配置信息"),
    ConnectInfoParseError("连接配置解析异常");

    final String text;
    LoginError(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean checkSuccess() {
        return this == Success;
    }

    public void show() {
        Alert alert = new Alert(Alert.AlertType.ERROR, this.text);
        alert.showAndWait();
    }
}

// 封装用于显示正在登陆的弹窗
class ConnectingAlert extends Alert {
    ButtonType exitType = new ButtonType("退出");
    String text = "正在连接";

    int count = 0;

    public ConnectingAlert(){
        super(AlertType.INFORMATION, "连接服务器，请勿关闭该窗口");
        this.setHeaderText(text);
        this.getButtonTypes().clear();
        this.getButtonTypes().addAll(exitType);

        new Thread(new WaitingTask()).start();
    }

    // 返回是否是手动关闭
    public boolean isExit() {
        return this.getResult() == exitType;
    }

    // 重新显示
    public void reshow() {
        this.show();
        new Thread(new WaitingTask()).start();
    }

    // 用于显示动态效果
    void setText() {
        while(this.isShowing()) {
//            System.out.println("[debug] waiting");
            // 添加延迟
            try { Thread.sleep(500); }
            catch(InterruptedException ignored){}

            if(count == 3) {
                Platform.runLater(()->this.setHeaderText(text));
                count = 0;
            } else {
                Platform.runLater(()->this.setHeaderText(this.getHeaderText() + "."));
                count++;
            }
        }
    }

    class WaitingTask extends Task<Void>{
        @Override
        protected Void call(){
            setText();
            return null;
        }
    }
}

// 封装连接失败的弹窗
class ConnectFailAlert extends Alert{
    ButtonType closeType = new ButtonType("关闭"),
        reconnectType = new ButtonType("重新连接");

    public ConnectFailAlert(){
        super(AlertType.ERROR, "连接服务器失败，请检查连接配置文件");
        this.getButtonTypes().clear();
        this.getButtonTypes().addAll(closeType, reconnectType);
    }

    // 提示连接错误
    public boolean isReconnect() {
        return this.getResult() == reconnectType;
    }
}