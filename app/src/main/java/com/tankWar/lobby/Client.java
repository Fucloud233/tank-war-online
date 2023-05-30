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
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class Client extends Application {

    // 连接相关的
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;

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

    // 聊天数据
    private String strSend;
    private String strReceive;
    private String strKey;
    private StringTokenizer st;
    //登录界面
    private Stage loginStage;
    //聊天室界面
    private Stage primaryStage;
    //测试的服务器

    @Override
    public void start(Stage primaryStage) {
        //绑定
        this.primaryStage=primaryStage;
        primaryStage.setTitle("Chat Client");

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

        borderPane.setCenter(txtViewTalk);
        borderPane.setBottom(hBox);

        // Button action
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                out.println("talk|" + txtTalk.getText() + "|" + txtName.getText() + "|" + listOnline.getValue());
                txtTalk.clear();
            }
        });

        //聊天窗口的场景
        Scene scene = new Scene(borderPane, 400, 300);
        primaryStage.setScene(scene);

        // 登录的UI
        txtServerIP = new TextField("192.168.47.1");
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
    }

    public static void main(String[] args) throws UnknownHostException {
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
                        while (st.hasMoreTokens()) {
                            String strOnline = st.nextToken();
                            Platform.runLater(() -> listOnline.getItems().add(strOnline));
                        }
                    } else if (strKey.equals("remove")) {
                        while (st.hasMoreTokens()) {
                            String strRemove = st.nextToken();
                            Platform.runLater(() -> listOnline.getItems().remove(strRemove));
                        }
                    } else if (strKey.equals("warning")) {
                        String strWarning = st.nextToken();
                        Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, strWarning).showAndWait());
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
