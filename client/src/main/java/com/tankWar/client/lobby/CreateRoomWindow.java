package com.tankWar.client.lobby;

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

    // 是否需要密码 (改成单选框)
    RadioButton hasPasswordButton;

    private Label pw;
    private PasswordField Password;
    private Button ackLogin;
    private GridPane createPane;
    private Stage createroomStage;

    private GameWaitWindow gameWaitWindow;//传递过来的游戏房间

    private Socket socket;

    //在构造函数中加上了初始化传递过来的舞台和场景
    public CreateRoomWindow(Socket s,String name,String account,GameWaitWindow gameWaitWindow) throws IOException{
        this.socket = s;
        this.username=name;
        this.account=account;
        this.gameWaitWindow=gameWaitWindow;
    }

    //在布局上添加了输入房间名和选择是否有密码
    public void ShowWindow(){
        roomName=new TextField("新游戏");
        volumnCB=new ComboBox<>();
        volumnCB.setItems(FXCollections.observableArrayList("2","3","4"));
        volumnCB.setValue("2");

        hasPasswordButton = new RadioButton("需要密码");
        hasPasswordButton.setSelected(false);

        Password = new PasswordField();
        pw = new Label("密码：");
        // [important] 初始化时设置隐藏
        Password.setVisible(false);
        pw.setVisible(false);

        createPane=new GridPane();
        createPane.setVgap(10);
        createPane.setHgap(10);
        createPane.setPadding(new Insets(20));
        createPane.addRow(0, new Label("房间名："), roomName);
        createPane.addRow(1, new Label("人数:"), volumnCB);
        createPane.add(hasPasswordButton,1,2);
        createPane.addRow(3, pw, Password);

        //列、行、列方向上跨度、行方向上跨度
        HBox hBox=new HBox();
        ackLogin=new Button("创建房间");
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().add(ackLogin);

        createPane.add(hBox, 1, 4);
        createPane.setPrefSize(300, 200);

        // 添加值变化监听器，根据选择的值来显示或隐藏密码框
        hasPasswordButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            boolean flag = hasPasswordButton.isSelected();
            Password.setVisible(flag);
            pw.setVisible(flag);
        });

        // 分设置密码和没有设置密码的两种情况,信息没填完整会有提示信息
        ackLogin.setOnAction(event ->{
            // 验证 1. 输入房间名 2. 密码
            boolean flag = hasPasswordButton.isSelected();
            if(roomName.getText().isEmpty() || (flag && Password.getText().isEmpty())) {
                new Alert(Alert.AlertType.WARNING,"请输入完整信息！").showAndWait();
                return;
            }

            // 生成发送消息
            String strSend = "Create|";
            strSend += flag ? "|password":"no password";
            strSend += "|"+username+"|"+account+"|"+roomName.getText()+"|"+volumnCB.getValue();
            strSend += flag ? "|"+Password.getText() : "";

            Communicate.send(socket, strSend);

            createroomStage.close();
            //设置这个人为房主
            gameWaitWindow.isRoomOwner=true;
            //打开游戏房间
            gameWaitWindow.ShowWindow();
        } );

        // 创建房间
        createroomStage=new Stage();
        createroomStage.setTitle("创建房间");
        createroomStage.setScene(new Scene(createPane));
        createroomStage.show();
    }

    public void CloseWindow(){
        createroomStage.close();
    }
}
