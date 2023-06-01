package com.tankWar.game.server;

public enum ServerPrompt {

    // Prompt
    RunSuccess("启动成功", 0),

    AllSuccess("全部成功", 0),
    AllConnected("客户端全部连接成功", 0),
    AllReceived("客户端消息全部接收成功", 0),
    AllSend("客户端消息全部发送成功", 0),

    // Error
    ReceiveFail("接收来自客户端的消息失败", 2),
    SendFail("向客户端发送消息失败", 2);



    String text;
    // 0 消息 1 警告 2 错误
    int type;

    ServerPrompt(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public void print() {
        String typeText = null;
        
        switch (type) {
            case 0->{typeText = "[Info] ";}
            case 1->{typeText = "[Warn] ";}
            case 2->{typeText = "[Error] ";}
        }

        System.out.println(typeText + text);
    }


}


