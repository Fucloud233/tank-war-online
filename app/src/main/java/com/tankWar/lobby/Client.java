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


public class Client extends Stage{

    // 连接相关的 由登录页面进行传入初始值
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    //聊天框界面的UI
    private TextField txtTalk;
    private ComboBox<String> listOnline;
    private TextArea txtViewTalk;
    private VBox container;
    private StringTokenizer st;

    private CreateRoomWindow roomWindow;
    private SelectRoomWindow selectRoomWindow;
    private GameWaitWindow gameWaitWindow;

    private final String username;   //登录时传过来的用户名
    private Stage primaryStage;

    public Client(String txtName,Socket socket,BufferedReader in,PrintWriter out){
        username=txtName;
        this.socket=socket;
        this.in=in;
        this.out=out;
    }

    public void RunClient() throws IOException {
        //绑定
        //聊天室界面
        Stage primaryStage = new Stage();
        primaryStage.setTitle("游戏大厅");

        BorderPane borderPane = new BorderPane();
        //游戏大厅的聊天框部分
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
        VBox container = new VBox();
        container.setPrefHeight(250); // 设置容器的固定高度
        ScrollPane scrollPane = new ScrollPane(container);//房间列表

        //创建房间按钮
        //游戏大厅的UI
        Button newRoomBtn = new Button("创建房间");
        newRoomBtn.setStyle("-fx-font: 16 arial; -fx-base: #b6e7c9;");

        //加入房间按钮
        Button enterRoomBtn = new Button("加入房间");
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
        borderPane.setCenter(container);
        borderPane.setBottom(BottomBox);

        Scene scene = new Scene(borderPane, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // 禁用窗口大小调整
        //显示界面
        btnTalk.setDisable(false);
        //创建一个线程来处理事件
        new Thread(new ClientThread()).start();
        out.println("init|online");
        primaryStage.show();

        //加入房间的按钮
        enterRoomBtn.setOnAction(e -> {
            //处理加入房间的按钮
            ///////////////////////////////////////////
        });

        // 聊天框中的按钮事件
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                //获取用户输入的账号
                out.println("talk|" + txtTalk.getText() + "|" + username + "|" + listOnline.getValue());
                txtTalk.clear();
            }
        });
        //创建房间按钮的事件
        newRoomBtn.setOnAction(e -> {
            //获取用户输入的账号
            try {
                roomWindow = new CreateRoomWindow(socket, username);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            //显示页面
            roomWindow.ShowWindow();
        });
    }

    class ClientThread implements Runnable {
        //这用来创建每一条房间的消息，显示在聊天框上面
        public HBox createHBox(String roomNum,int enterNum,int userNum) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);
            hBox.setPadding(new Insets(10));

            Label roomLabel = new Label(roomNum + "的房间");
            Label countLabel = new Label("人数：" + enterNum+"/"+userNum);
            Button enterBtn = new Button();
            ///////////////////////////????这样子的逻辑
            if (enterNum<userNum){
                enterBtn.setText("进入");
            }else {
                enterBtn.setText("游戏中");
            }

            enterBtn.setOnAction(event -> {
                if (enterBtn.getText().equals("进入")){
                    try {
                        selectRoomWindow=new SelectRoomWindow(socket,roomNum,username);
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
                    String strReceive = in.readLine();
                    st = new StringTokenizer(strReceive, "|");
                    String strKey = st.nextToken();
                    //截取消息 显示对应的内容
                    switch (strKey) {
                        //游戏大厅部分聊天框的聊天内容
                        case "talk" -> {
                            String strTalk = st.nextToken();
                            //显示聊天的内容
                            Platform.runLater(() -> txtViewTalk.appendText("\n" + strTalk));
                        }
                        //游戏大厅部分聊天框中在线用户的更新
                        case "online" -> Platform.runLater(() -> {
                            listOnline.getItems().clear();
                            listOnline.getItems().add("All");
                            while (st.hasMoreTokens()) {
                                String strOnline = st.nextToken();
                                listOnline.getItems().add(strOnline);
                            }
                        });
                        //登陆失败提示
                        case "warning" -> {
                            String strWarning = st.nextToken();
                            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, strWarning).showAndWait());
                        }
                        //创建房间的信息
                        case "Create" -> {
                            while (st.hasMoreTokens()) {
                                String strCreate = st.nextToken();
                                if (strCreate.equals("Failed")) {
                                    Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "你已经创建过房间了！").showAndWait());
                                } else {
                                    Platform.runLater(() -> {
                                        roomWindow.CloseWindow();
                                    });
                                }
                            }
                        }
                        //选择加入房间
                        case "select room" -> {
                            String strSelect = st.nextToken();
                            if (strSelect.equals("password error")) {
                                Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "密码错误！").showAndWait());
                            } else if (strSelect.equals("success")) {
                                Platform.runLater(() -> {
                                    selectRoomWindow.CloseWindow();
                                    primaryStage.close();
                                    try {
                                        gameWaitWindow = new GameWaitWindow(socket, username);
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
                        }
                        //在进入房间后的聊天框的聊天内容
                        case "roomTalk" -> {
                            String strTalk = st.nextToken();
                            Platform.runLater(() -> gameWaitWindow.AddTxt("\n" + strTalk));
                            Platform.runLater(() -> txtViewTalk.appendText("\n" + strTalk));
                        }
                        //在进入房间后的，更新房间里面的在线用户
                        case "room online" ->
                                Platform.runLater(() -> {
                                    gameWaitWindow.ClearTalkTo();
                                    gameWaitWindow.AddTalkTo("All");
                                    while (st.hasMoreTokens()) {
                                        String strOnline = st.nextToken();
                                        gameWaitWindow.AddTalkTo(strOnline);
                                    }
                                });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
