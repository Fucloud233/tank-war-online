package com.tankWar.lobby;

import com.tankWar.communication.Communicate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;

import static javafx.scene.paint.Color.GREEN;



public class GameWaitWindow {
    //连接相关
    Socket socket;

    //聊天框界面的UI
    private TextField txtTalk;
    private TextArea txtViewTalk;
    private Button btnTalk;
    private ComboBox<String> listOnline;
    //房间里放置玩家信息的UI
    public String name;//这个就是用户的名字
    private String account;//用户的账号
    private StringTokenizer st;
    private Scene roomScene;//自己的场景
    private Scene lobbyScene;//接收传过来的场景
    private Stage primaryStage;//接收传过来的主舞台
    public Boolean isRoomOwner = false;   //是否为房主 在CreateRoomWindow中将其设置为true
    private Button PlayGameBtn;  //开始游戏/准备按钮
    Button exitRoomBtn;

    TableView<UserInfo> userTableView = new TableView<>();
    ObservableList<UserInfo> data = FXCollections.observableArrayList();

    public GameWaitWindow(Socket s, String name, String account, Stage primaryStage, Scene lobbyScene) throws IOException {
        this.socket = s;
        this.name = name;
        this.account = account;
        this.primaryStage = primaryStage;
        this.lobbyScene = lobbyScene;
    }

    //存储用户的信息
    public static class UserInfo{
        StringProperty ID;
        StringProperty username;
        StringProperty status;

        public UserInfo(String ID, String username, String status) {
            this.ID = new SimpleStringProperty(ID);
            this.username = new SimpleStringProperty(username);
            this.status = new SimpleStringProperty(status);
        }
        public String getID() {
            return ID.get();
        }

        public String getUsername() {
            return username.get();
        }

        public String getStatus() {
            return status.get();
        }

    }

    void ShowWindow(){
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
        //调整聊天内容的高度
        txtViewTalk.setPrefHeight(500);


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
        TableColumn<UserInfo, String> idColumn = new TableColumn<>("序号");
        TableColumn<UserInfo, String> usernameColumn = new TableColumn<>("昵称");
        TableColumn<UserInfo, String> statusColumn = new TableColumn<>("状态");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 设置列宽
        idColumn.prefWidthProperty().bind(userTableView.widthProperty().multiply(0.33));
        usernameColumn.prefWidthProperty().bind(userTableView.widthProperty().multiply(0.33));
        statusColumn.prefWidthProperty().bind(userTableView.widthProperty().multiply(0.34));

        // 设置单元格工厂
        Callback<TableColumn<UserInfo, String>, TableCell<UserInfo, String>> cellFactory =
                column -> new TableCell<UserInfo, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item);
//                            setFont();
                            setStyle("-fx-font-size: 18px;");  // 设置字体大小为18像素
                        }
                    }
                };

        idColumn.setCellFactory(cellFactory);
        usernameColumn.setCellFactory(cellFactory);
        statusColumn.setCellFactory(cellFactory);


        // 设置行工厂，用于自定义行的外观和行为
        userTableView.setRowFactory(tv -> {
            TableRow<UserInfo> row = new TableRow<>();
            row.setPrefHeight(40);  // 设置行高，这里设置为30像素
            return row;
        });

        userTableView.getColumns().addAll(idColumn, usernameColumn, statusColumn);


        //绑定数据
        userTableView.setItems(data);

        //修改边框颜色
        userTableView.setStyle("-fx-background-color: #494f3c;-fx-control-inner-background:green");
        // 设置userListView的样式——内部颜色
        userTableView.setStyle("-fx-control-inner-background: #494f3c;");


/*
        // 创建一个ListView来显示房间内的用户   在下面对聊天对象更新的函数中进行更新ListView
        userListView = new ListView<>(FXCollections.observableArrayList(listOnline.getItems()).filtered(item -> !item.equals("All")));

        // 设置ListView的最大显示行数为4行
        int maxRows = 5;
        int itemHeight = 60; // 设置每个条目的高度（根据实际情况调整）
        userListView.setPrefHeight(maxRows * itemHeight);
        userListView.setPadding(new Insets(5));
        //修改边框颜色
        userListView.setStyle("-fx-background-color: #494f3c;-fx-control-inner-background:green");
        /////////////////////房间内用户——样式的修改/////////////////
        // 设置userListView的样式——内部颜色
        userListView.setStyle("-fx-control-inner-background: #494f3c;");
        // 设置userListView的单元格样式
        userListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item);
                    //字体的调整
                    setFont(Font.font("Arial", FontWeight.BOLD, 20));
                    setTextFill(Color.WHITE);
                    setBackground(new Background(new BackgroundFill(Color.web("#494f3c"), null, null)));
                } else {
                    setText(null);
                }
            }
        });
        userListView.setDisable(true);

        */


        txtViewTalk.setPadding(new Insets(5));
        // 设置txtViewTalk的样式
        txtViewTalk.setStyle("-fx-control-inner-background: #494f3c; -fx-text-fill: white;-fx-font-size: 15");


        //将用户列表装进Box中
        userListBox.getChildren().add(userTableView);
        //设置一个新的VBox 装载userList和BottomBox
        VBox newVBox=new VBox();
        newVBox.getChildren().add(userListBox);
        newVBox.getChildren().add(BottomBox);
        borderPane.setCenter(newVBox);
        // 设置迷彩背景
        borderPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #4D774E, #9C8B56, #614D79);");
        // 设置聊天框的输入框样式
        txtTalk.setStyle("-fx-background-color: #494f3c; -fx-text-fill: white;");
        txtTalk.setStyle("-fx-prompt-text-fill: white;");

        // 设置聊天框的发送按钮样式
        btnTalk.setStyle("-fx-base: #b6e7c9; -fx-text-fill: black;");

        // 设置开始游戏/准备按钮样式
        PlayGameBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9; -fx-text-fill: black;");

        // 设置退出房间按钮样式
        exitRoomBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9; -fx-text-fill: black;");

        //场景切换
        roomScene = new Scene(borderPane, 800, 700);
        // 添加像素字体
        URL styleURL = this.getClass().getResource("/css/label.css");
        if(styleURL != null)
            roomScene.getStylesheets().add(styleURL.toExternalForm());

        primaryStage.setTitle("游戏房间");
        roomScene.setFill(GREEN);
        primaryStage.setScene(roomScene);

        //发送消息按钮的触发
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                if (isRoomOwner){
                    if(listOnline.getValue()==null) //未选中默认为和全部人说
                    {
                        System.out.println("3");
                        Communicate.send(socket,  "roomTalk|" + txtTalk.getText() + "|" + name+ "|" + "All");
                    }
                    else{
                        System.out.println("4");
                        Communicate.send(socket,  "roomTalk|" + txtTalk.getText() + "|" + name + "|" + listOnline.getValue());
                    }
                }else {
                    if(listOnline.getValue()==null) //未选中默认为和全部人说
                    {
                        System.out.println("2");
                        //获取用户输入的账号
                        Communicate.send(socket,  "roomTalk|" + txtTalk.getText() + "|" + name+"|" + "All");
                    }
                    else{
                        //获取用户输入的账号
                        /*if((name + "*已准备").equals(listOnline.getValue()) || (name + "*未准备").equals(listOnline.getValue())){
                            Communicate.send(socket,  "roomTalk|" + txtTalk.getText() + "|" + name + "|" + name);
                        }
                        else{
                            Communicate.send(socket,  "roomTalk|" + txtTalk.getText() + "|" + name + "|" + listOnline.getValue());
                        }*/
                        System.out.println("1");
                        Communicate.send(socket,  "roomTalk|" + txtTalk.getText() + "|" + name + "|" + listOnline.getValue());
                    }
                }
                txtTalk.clear();
            }
        });

    }


    void AddTalkTo(String strOnline) {
        // 添加用户名称到listOnline中
        listOnline.getItems().add(strOnline);
        listOnline.setValue("All"); // 设置"ALL"为默认选项
    }

    void newAddTalkTo(int id, String name, String status){
        // 添加数据测试
        data.add(new UserInfo(String.valueOf(id), name, status));
        System.out.println("[test] data len: " + data.size());
    }

    void ClearTalkTo() {
        // 清空房间内用户
        listOnline.getItems().clear();
        //清空数据
        data.clear();

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
            //清空数据信息
            data.clear();
            userTableView.getColumns().clear();
            userTableView.getItems().clear();
        }

    }

    //开始游戏  或 进行准备的事件
    public void beginGameAction() throws IOException {
        //如果房主点击开始游戏  需要检查所有用户的状态是否已经准备
        if (isRoomOwner) {
            int playerCount = listOnline.getItems().size() - 1;
            System.out.println("count"+playerCount);
            if(playerCount==1){
                //房主一个人无法开始游戏 进行提示
                new Alert(Alert.AlertType.WARNING,"您无法一个人开始游戏！").showAndWait();
            }
            else {
                //否则 检查房间中用户的状态是否都准备好
                checkAllPlayersReady();
            }
        }
        //普通用户点击准备按钮，按钮切换为已经准备 ，并且在列表中也进行切换
        else if (PlayGameBtn.getText().equals("准备")) {
            //发送一个准备的消息 并传送这个能标识这个用户的键
            Communicate.send(socket, "isReady|" + name);
            System.out.println("[info] name: "+name);
            // 非房主用户 切换按钮和对应的状态 准备-已准备
            PlayGameBtn.setText("取消准备");
        }
        //用户取消准备
        else if(PlayGameBtn.getText().equals("取消准备")){
            //发送一个取消准备的消息 并传送这个能标识这个用户的键
            Communicate.send(socket, "cancelReady|" + name);
            // 非房主用户 切换按钮和对应的状态 已准备-准备
            PlayGameBtn.setText("准备");
        }
    }

    //检查是否所有的用户都已经准备好
    private void checkAllPlayersReady() {
        //发送给服务器检查房间内用户是否全部准备好的信息
        Communicate.send(socket, "check status");
    }

    // 开始游戏后，设置按钮不可触发，设置取消准备，以确保房主先出来不能开始游戏
    public void changeStatus(String status)  {
        switch (status) {
            case "play" -> {
//                this.exitRoomBtn.setDisable(true);
                if (!isRoomOwner) {
                    this.PlayGameBtn.setText("准备");
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
