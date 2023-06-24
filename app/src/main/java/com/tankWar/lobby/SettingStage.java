package com.tankWar.lobby;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// 不再使用设置窗口

public class SettingStage {
    Stage stage = new Stage();
    VBox mainPane = new VBox();

    String address;
    int port;

    public SettingStage(String address, int port) {
        this.address = address;
        this.port = port;

        this.init();
    }

    void init() {
        Label addressLabel = new Label("IP地址");
        Label portLabel  = new Label("端口");

        TextField addressInput = new TextField(address);
        TextField portInput = new TextField(Integer.toString(port));

        Button cancelButton = new Button("取消");
        Button okButton = new Button("确定");

        // 确定按钮
        okButton.setOnAction((e)->{
            // 读取信息
            String inputAddress = addressInput.getText();
            int inputPort = Integer.parseInt(portInput.getText());

            address = inputAddress;
            port = inputPort;

            new ConnectAlert().show();
            // 关闭窗口
//            stage.close();
        });

        // 取消按钮
        cancelButton.setOnAction((e)->stage.close());

        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(cancelButton, okButton);

        mainPane.setPadding(new Insets(10));
        mainPane.setSpacing(10);
        mainPane.getChildren().addAll(addressLabel, addressInput, portLabel, portInput, buttons);

        Scene scene = new Scene(mainPane);
        stage.setWidth(LobbyConfig.LoginStageWidth);
        stage.setHeight(LobbyConfig.LoginStageHeight);
        stage.setResizable(false);
        stage.setTitle("设置");
        stage.setScene(scene);
    }

    public void showAndWait() {
        this.stage.showAndWait();
    }

    // 获取IP地址
    public String getAddress() {
        return address;
    }

    // 获取端口
    public int getPort() {
        return port;
    }
}

class ConnectAlert extends Alert {

    public ConnectAlert() {
        super(AlertType.INFORMATION);
        this.setHeaderText("正在连接中");
        this.initStyle(StageStyle.UNDECORATED);
    }
}