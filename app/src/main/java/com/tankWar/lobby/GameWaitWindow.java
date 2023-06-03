package com.tankWar.lobby;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
public class GameWaitWindow {
    Socket socket = new Socket();
    BufferedReader in = null;
    PrintWriter out = null;
    //聊天框界面的UI
    private TextField txtTalk;
    private TextArea txtViewTalk;
    private Button btnTalk;
    private ComboBox<String> listOnline;
    //房间里放置玩家信息的UI
    private VBox container;

    private String name;//这个就是用户的名字
    private String account;//用户的账号
    private StringTokenizer st;
    private Scene roomScene;//自己的场景
    private Scene lobbyScene;//接收传过来的场景
    private Stage primaryStage;//接收传过来的主舞台
    public GameWaitWindow(Socket s,String name,String account,Stage primaryStage,Scene lobbyScene) throws IOException{
        this.socket = s;
        this.name=name;
        this.account=account;
        this.primaryStage=primaryStage;
        this.lobbyScene=lobbyScene;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

    }
    void ShowWindow(){
        BorderPane borderPane = new BorderPane();
        //游戏房间的聊天框部分
        txtTalk = new TextField();   //编辑发送内容
        txtViewTalk = new TextArea();   //查看聊天内容
        Button btnTalk = new Button("发送");  //发送按钮
        listOnline = new ComboBox<>();  //选择发送的对象
        listOnline.getItems().add("All");  //添加在线人员列表
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
        container = new VBox();
        container.setPrefHeight(250); // 设置容器的固定高度
        ScrollPane scrollPane = new ScrollPane(container);//房间列表

        //创建房间按钮
        Button newRoomBtn = new Button("开始游戏");
        newRoomBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");

        //加入房间按钮
        Button enterRoomBtn = new Button("退出游戏");
        enterRoomBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");

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
        borderPane.setCenter(scrollPane);
        borderPane.setBottom(BottomBox);
        //场景切换
        roomScene=new Scene(borderPane,800,700);
        primaryStage.setTitle("游戏房间");
        primaryStage.setScene(roomScene);

        //发送消息按钮的触发
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                //获取用户输入的账号
                out.println("roomTalk|" + txtTalk.getText() + "|" + name + "|" + listOnline.getValue());
                txtTalk.clear();
            }
        });

    }

    void AddTalkTo(String strOnline){
        listOnline.getItems().add(strOnline);
    }
    void ClearTalkTo(){
        listOnline.getItems().clear();
    }
    void AddTxt(String strTalk){
        txtViewTalk.appendText("\n"+strTalk);
    }



}
