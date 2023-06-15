package com.tankWar.lobby;

import com.tankWar.communication.Communicate;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;



public class CreateRoomWindow{
    private String strsend;
    private String username;//房主名字
    private String account;//房主账号
    private TextField roomName;//房间名字
    private ComboBox<String> volumnCB ;//房间人数
    private ComboBox<String> setpw;//是否要密码
    private PasswordField Password;
    private Button ackLogin;
    private GridPane createPane;
    private Stage createroomStage;

    private GameWaitWindow gameWaitWindow;//传递过来的游戏房间

    private Socket socket=new Socket();
    DataInputStream in = null;
    DataOutputStream out = null;
    //在构造函数中加上了初始化传递过来的舞台和场景
    public CreateRoomWindow(Socket s,String name,String account,GameWaitWindow gameWaitWindow) throws IOException{
        this.socket = s;
        this.username=name;
        this.account=account;
        this.gameWaitWindow=gameWaitWindow;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }
    //在布局上添加了输入房间名和选择是否有密码
    public void ShowWindow(){
        roomName=new TextField();
        volumnCB=new ComboBox<>();
        volumnCB.setItems(FXCollections.observableArrayList("2","3","4"));
        volumnCB.setValue("2");
        setpw=new ComboBox<>(FXCollections.<String>observableArrayList("否", "是"));
        setpw.setValue("否");
        Password=new PasswordField();
        createroomStage=new Stage();
        createroomStage.setTitle("创建房间");
        createPane=new GridPane();
        createPane.setVgap(10);
        createPane.setHgap(10);
        createPane.setPadding(new Insets(20));
        createPane.add(new Label("房间名："),0,0);
        createPane.add(roomName,1,0);
        createPane.add(new Label("人数:"), 0, 1);
        createPane.add(volumnCB, 1, 1);
        createPane.add(new Label("是否设置密码："),0,2);
        createPane.add(setpw,1,2);
        createPane.add(new Label("密码:"), 0, 3);
        createPane.add(Password, 1, 3);
        //列、行、列方向上跨度、行方向上跨度
        HBox hBox=new HBox();
        ackLogin=new Button("确认");
        //分设置密码和没有设置密码的两种情况,信息没填完整会有提示信息
        ackLogin.setOnAction(event ->{
            if (setpw.getValue().equals("是")){
                if (!roomName.getText().isEmpty()&&!Password.getText().isEmpty()){
                    strsend="Create|password"+"|"+username+"|"+account+"|"+roomName.getText()+"|"+volumnCB.getValue()+"|"+Password.getText();
                    Communicate.send(socket, strsend);
                    createroomStage.close();
                    //设置这个人为房主
                    gameWaitWindow.isRoomOwner=true;
                    //打开游戏房间
                    gameWaitWindow.ShowWindow();
                }
            }else if (setpw.getValue().equals("否")){
                if (!roomName.getText().isEmpty()){
                    strsend="Create|no password"+"|"+username+"|"+account+"|"+roomName.getText()+"|"+volumnCB.getValue();
                    Communicate.send(socket, strsend);
                    createroomStage.close();
                    //设置这个人为房主
                    gameWaitWindow.isRoomOwner=true;
                    //打开游戏房间
                    gameWaitWindow.ShowWindow();
                }
            }else {
                new Alert(Alert.AlertType.WARNING,"请输入完整信息！").showAndWait();
            }

        } );
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(ackLogin);
        createPane.add(hBox,0,4,2,1);
        BorderPane borderPane=new BorderPane();
        borderPane.setCenter(createPane);
        createroomStage.setScene(new Scene(borderPane));
        createroomStage.show();
    }


    public void CloseWindow(){
        createroomStage.close();
    }


}
