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
    // 聊天数据
    private String strSend;
    private String strReceive;
    private String strKey;
    private String name;
    private StringTokenizer st;
    private Runnable onClosedCallback;
    //聊天室界面
    private Stage primaryStage=new Stage();

    public GameWaitWindow(Socket s,String name) throws IOException{
        this.socket = s;
        this.name=name;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

    }
    void ShowWindow(){
        primaryStage.setTitle("游戏房间");

        txtTalk = new TextField();
        txtViewTalk = new TextArea();
        btnTalk = new Button("发送");
        btnTalk.setOnAction(e -> {
            if (!txtTalk.getText().isEmpty()) {
                out.println("roomTalk|" + txtTalk.getText() + "|" + name + "|" + listOnline.getValue());
                txtTalk.clear();
            }
        });

        listOnline = new ComboBox<>();
        listOnline.getItems().add("All");

        txtViewTalk.setEditable(false);
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(10));
        hBox.getChildren().addAll(new Label("Talk:"), txtTalk, new Label("To:"), listOnline, btnTalk);
        VBox vBox=new VBox();
        vBox.getChildren().add(txtViewTalk);
        vBox.getChildren().add(hBox);
        HBox hBox1=new HBox();
        hBox1.setAlignment(Pos.CENTER);
        Button beginBtn=new Button("开始游戏");
        beginBtn.setPrefHeight(50);
        beginBtn.setPrefWidth(100);
        Button exitBtn=new Button("退出游戏");
        exitBtn.setPrefHeight(50);
        exitBtn.setPrefWidth(100);
        exitBtn.setOnAction(event ->{
            String strExit="exit";
            out.println(strExit);

            // 关闭新页面
            primaryStage.close();

            // 调用回调函数，回到上一个页面
            if (onClosedCallback != null) {
                onClosedCallback.run();
            }
        });

        hBox1.getChildren().addAll(beginBtn,new Label("       "),exitBtn);
        BorderPane borderPane = new BorderPane();
        borderPane.setBottom(vBox);
        borderPane.setCenter(hBox1);
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.show();
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
    void RemoveTalkTo(String strTalk){
        listOnline.getItems().remove(strTalk);
    }
    public void setOnClosedCallback(Runnable callback) {
        this.onClosedCallback = callback;
    }

}
