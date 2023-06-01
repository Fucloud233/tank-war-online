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
import java.util.StringTokenizer;

import static java.lang.System.*;


public class CreateRoomWindow{
    private String RoomNum;
    private String volumn;
    private String pw;
    private String strsend;
    private String strReceived;
    private String strKey;
    private StringTokenizer st;

    //    private ComboBox<String> RoomNumCB;
    private String txtName;
    private ComboBox<String> volumnCB ;
    private PasswordField Password;
    private Button ackLogin;
    private GridPane createPane;
    private Stage createroomStage;
    private Socket socket=new Socket();
    BufferedReader in = null;
    PrintWriter out = null;

    public CreateRoomWindow(Socket s,String name) throws IOException{
        this.socket = s;
        txtName=name;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

    }

    public void ShowWindow(){
//       RoomNumCB=new ComboBox<>();
//       RoomNumCB.setItems(FXCollections.observableArrayList("0","1","2","3","4","5","6","7","8"
//       ,"9","10","11"));
//       RoomNumCB.setValue("0");
        volumnCB=new ComboBox<>();
        volumnCB.setItems(FXCollections.observableArrayList("2","3","4"));
        volumnCB.setValue("2");
        Password=new PasswordField();
        createroomStage=new Stage();
        createroomStage.setTitle("创建房间");
        createPane=new GridPane();
        createPane.setVgap(10);
        createPane.setHgap(10);
        createPane.setPadding(new Insets(20));
//        createPane.add(new Label("房间号:"), 0, 0);
//        createPane.add(RoomNumCB, 1, 0);
        createPane.add(new Label("人数:"), 0, 0);
        createPane.add(volumnCB, 1, 0);
        createPane.add(new Label("密码:"), 0, 1);
        createPane.add(Password, 1, 1);
        //列、行、列方向上跨度、行方向上跨度
        HBox hBox=new HBox();
        ackLogin=new Button("确认");

        ackLogin.setOnAction(event ->{
            if (!Password.getText().isEmpty()){
                strsend="Create|"+txtName+"|"+volumnCB.getValue()+"|"+Password.getText();
                out.println(strsend);
//                try {
//                    in.readLine();
//                    System.out.println("Create OK");
//
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                System.out.println(strsend);
//                try {
//                    strReceived=in.readLine();
//                    System.out.println("OK");
//
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                st = new StringTokenizer(strReceived, "|");
//                strKey=st.nextToken();
//                if (strKey.equals("Create")){
//                    System.out.println("create ok");
//                    CloseWindow();
//                }


            }
        } );
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(ackLogin);
        BorderPane borderPane=new BorderPane();
        borderPane.setCenter(createPane);
        borderPane.setBottom(hBox);
        createroomStage.setScene(new Scene(borderPane));
        createroomStage.show();
    }
//    public String getStrmes(){
//        return strmes;
//    }

    public void CloseWindow(){
        createroomStage.close();
    }
}
