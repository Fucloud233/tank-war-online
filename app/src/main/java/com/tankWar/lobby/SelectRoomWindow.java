package com.tankWar.lobby;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class SelectRoomWindow {
    private String room;
    private String user;
    private PasswordField Password;
    private Label label;
    private HBox hBox;
    private VBox vBox;
    private Button button;
    private Stage selectroomstage;

    private Socket socket=new Socket();
    PrintWriter out = null;

    public SelectRoomWindow(Socket s,String a,String b) throws IOException {
        this.socket = s;
        room=a;
        user=b;
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public void ShowWindow(){
        Password=new PasswordField();
        label=new Label("房间密码");
        hBox=new HBox(label,Password);
        button=new Button("确认");
        vBox=new VBox(hBox,button);
        button.setOnAction(event -> {
            String s =Password.getText();
            out.println("select room|"+room+"|"+s+"|"+user);
        });
        selectroomstage=new Stage();
        selectroomstage.setTitle("输入房间密码");
        selectroomstage.setScene(new Scene(vBox));
        selectroomstage.show();

    }

    public void CloseWindow(){
        selectroomstage.close();
    }

}
