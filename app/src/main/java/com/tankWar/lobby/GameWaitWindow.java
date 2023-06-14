package com.tankWar.lobby;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.StringTokenizer;

public class GameWaitWindow {
    ListView<String> userListView;
    //连接相关
    Socket socket = new Socket();
    DataInputStream in = null;
    DataOutputStream out = null;
    //聊天框界面的UI
    private TextField txtTalk;
    private TextArea txtViewTalk;
    private Button btnTalk;
    private ComboBox<String> listOnline;
    //房间里放置玩家信息的UI
    private String name;//这个就是用户的名字
    private String account;//用户的账号
    private StringTokenizer st;
    private Scene roomScene;//自己的场景
    private Scene lobbyScene;//接收传过来的场景
    private Stage primaryStage;//接收传过来的主舞台
    public Boolean isRoomOwner = false;   //是否为房主 在CreateRoomWindow中将其设置为true
    private Button PlayGameBtn;  //开始游戏/准备按钮
    Button exitRoomBtn;

    public GameWaitWindow(Socket s, String name, String account, Stage primaryStage, Scene lobbyScene) throws IOException {
        this.socket = s;
        this.name = name;
        this.account = account;
        this.primaryStage = primaryStage;
        this.lobbyScene = lobbyScene;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    void ShowWindow() {
        //打印是否为房主进行测试
        System.out.println("[info] 是否房主 "+isRoomOwner);

        BorderPane borderPane = new BorderPane();
        //游戏房间的聊天框部分
        txtTalk = new TextField();   //编辑发送内容
        txtViewTalk = new TextArea();   //查看聊天内容
        Button btnTalk = new Button("发送");  //发送按钮
        // 初始化listOnline，并添加"All"
        listOnline = new ComboBox<>();
        listOnline.getItems().add("All");
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

        //开始游戏/准备按钮
        PlayGameBtn = new Button(isRoomOwner ? "开始游戏" : "准备");
        PlayGameBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");
        PlayGameBtn.setOnAction(e -> {
            try {
                beginGameAction();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }); //退出房间的事件

        //退出房间按钮
        exitRoomBtn = new Button("退出房间");
        exitRoomBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");
        exitRoomBtn.setOnAction(e -> {
            try {
                exitRoom();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }); //退出房间的事件

        //装填按钮的盒子
        HBox ButtonBox = new HBox();
        ButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        ButtonBox.setSpacing(25);  //按钮间距
        ButtonBox.setPadding(new Insets(10));
        //将按钮放置在一起
        ButtonBox.getChildren().addAll(exitRoomBtn, PlayGameBtn);
        // 设置 ButtonBox 样式
        ButtonBox.setStyle("-fx-border-color: black; -fx-border-width: 3px;");

        //将按钮与聊天框放置在一起
        VBox BottomBox = new VBox();
        BottomBox.getChildren().add(ButtonBox);
        BottomBox.getChildren().add(vBox);

        VBox userListBox = new VBox();
        // 创建一个ListView来显示房间内的用户   在下面对聊天对象更新的函数中进行更新ListView
        userListView = new ListView<>(FXCollections.observableArrayList(listOnline.getItems()).filtered(item -> !item.equals("All")));

        // 设置ListView的最大显示行数为4行
        int maxRows = 4;
        int itemHeight = 30; // 设置每个条目的高度（根据实际情况调整）
        userListView.setPrefHeight(maxRows * itemHeight);
        //将用户列表装进Box中
        userListBox.getChildren().add(userListView);

        //最后将组件排布在borderPane上
        borderPane.setCenter(userListBox);
        borderPane.setBottom(BottomBox);
        //场景切换
        roomScene = new Scene(borderPane, 800, 700);
        primaryStage.setTitle("游戏房间");
        primaryStage.setScene(roomScene);

        //发送消息按钮的触发
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                //获取用户输入的账号
                Communicate.send(socket,  "roomTalk|" + txtTalk.getText() + "|" + name + "|" + listOnline.getValue());
                txtTalk.clear();
            }
        });

    }

    void AddTalkTo(String strOnline) {
        listOnline.getItems().add(strOnline);
        //将用户列表进行放置
        userListView.setItems(FXCollections.observableArrayList(listOnline.getItems()).filtered(item -> !item.equals("All")));
    }

    void ClearTalkTo() {
        listOnline.getItems().clear();
        userListView.setItems(FXCollections.observableArrayList());
    }

    void AddTxt(String strTalk) {
        txtViewTalk.appendText("\n" + strTalk);
    }

    //退出房间的事件
    public void exitRoom() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText(null);
        if (isRoomOwner) {
            //如果是房主
            alert.setContentText("您会解散该房间，确定吗？");
        } else {
            //普通用户
            alert.setContentText("确定要退出房间吗？");
        }
        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmButtonType, cancelButtonType);
        //等待用户选择按钮
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == confirmButtonType) {
            //发送给服务器退出房间的信息 在服务器端处理退出房间的逻辑
            Communicate.send(socket, "exitRoom");
            primaryStage.setTitle("游戏大厅");
            primaryStage.setScene(lobbyScene);
        }

    }

    //开始游戏  或 进行准备的事件
    public void beginGameAction() throws IOException {
        //如果房主点击开始游戏  需要检查所有用户的状态是否已经准备
        if (isRoomOwner) {
            checkAllPlayersReady();
        }
        //普通用户点击准备按钮，按钮切换为已经准备 ，并且在列表中也进行切换
        else if (PlayGameBtn.getText().equals("准备")) {
            //发送一个准备的消息 并传送这个能标识这个用户的键
            Communicate.send(socket, "isReady|" + name);
            System.out.println("[info] name: "+name);
            // 非房主用户 切换按钮和对应的状态 准备-已准备
            PlayGameBtn.setText("已准备");
        }
        //用户取消准备
        else if (PlayGameBtn.getText().equals("已准备")) {
            //发送一个取消准备的消息 并传送这个能标识这个用户的键
            Communicate.send(socket, "cancelReady|" + name);
            // 非房主用户 切换按钮和对应的状态 已准备-准备
            PlayGameBtn.setText("准备");
        }
    }

    //检查是否所有的用户都已经准备好
    private void checkAllPlayersReady() throws IOException {
        //发送给服务器检查房间内用户是否全部准备好的信息
        Communicate.send(socket, "check status");
    }

    // 开始游戏后，设置按钮不可触发，设置取消准备，以确保房主先出来不能开始游戏
    public void changeStatus(String status) throws IOException {
        switch (status) {
            case "play" -> {
//                this.exitRoomBtn.setDisable(true);
//                this.PlayGameBtn.setDisable(true);
                if (!isRoomOwner) {
                    // Add your code to start the game here
                    // For example:
                    System.out.println("[info] Game started!");
                }
            }
            case "ready" -> {
//                this.exitRoomBtn.setDisable(false);
//                this.PlayGameBtn.setDisable(false);

            }
        }
    }
}
