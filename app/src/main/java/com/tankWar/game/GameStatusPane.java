package com.tankWar.game;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class GameStatusPane extends GridPane {
    // 用于记录状态类型
    public enum StatusType{
        TotalGameNum(0, "总局数"),
        CurGameNum(1, "当前局数"),
        TotalPlayerNum(2, "总玩家数"),
        RestPlayerNum(3, "剩余玩家数"),
        LastType(4, "");

        final int value;
        final String text;

        StatusType(int value, String text) {
            this.value = value;
            this.text = text;
        }

        public int getValue() {
            return value;
        }

        public String getText() {
            return text;
        }
    }

    final int StatusNum = StatusType.LastType.value;

    // 初始化数组
    Label[] fieldLabels = new Label[StatusNum];
    Label[] valueLabels = new Label[StatusNum];
    Object[] values = new Object[StatusNum];

    public GameStatusPane() {
        init();
    }

    void init() {
        for(int i = 0; i<StatusNum; i++) {
            values[i] = new Object();

            // 设置字段名
            fieldLabels[i] = new Label(StatusType.values()[i].getText());
            // 设置值
            valueLabels[i] = new Label(values[i].toString());
        }

        this.addColumn(0, fieldLabels);
        this.addColumn(1, valueLabels);
        this.setHgap(10);
        this.setVgap(5);


        this.setStyle("-fx-font-size: 16px;");
    }

    public void setValue(StatusType type, Object value) {
        int i = type.getValue();
        values[i] = value;
        valueLabels[i].setText(value.toString());
    }

    public Object getValue(StatusType type) {
        return values[type.getValue()];
    }

    public void decResetPlayerNum() {
        int i = StatusType.RestPlayerNum.value;
        setValue(StatusType.RestPlayerNum,  (int) values[i] - 1);
    }

    // 用于设置属性
    public void setTotalGameNum(int num) {
        setValue(StatusType.TotalGameNum, num);
    }

    public void setCurGameNum(int num) {
        setValue(StatusType.CurGameNum, num);
    }

    public void setRestPlayerNum(int num) {
        setValue(StatusType.RestPlayerNum, num);
    }

    public void setTotalPlayerNum(int num) {
        setValue(StatusType.TotalPlayerNum, num);
    }

}
