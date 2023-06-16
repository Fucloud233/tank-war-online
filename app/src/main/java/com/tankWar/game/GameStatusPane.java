package com.tankWar.game;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Vector;

import static com.tankWar.game.StatusType.*;

public class GameStatusPane extends VBox {
    // 包含的组件信息
    Label statusTitle = new Label("游戏状态");
    TablePane statusTable = new TablePane();


    public GameStatusPane() {
        init();
    }

    // 初始化上述组件信息
    void init() {
        statusTable.addMultipleStatus(GameNum, 5);
        statusTable.addMultipleStatus(PlayerNum, 4);

        this.getChildren().addAll(statusTitle, statusTable);

        // 设置样式
        this.setPadding(new Insets(5));
        this.setStyle("-fx-font-size: 16px;");
    }

    /* 封装好的属性设置函数 */
    public void decResetPlayerNum() {
//        MultipleStatusLabel label = (MultipleStatusLabel) this.controls.get(PlayerNum);
//        int num = label.getRValue();
//        label.setRValue(num - 1);
    }



    /* 底层的属性设置函数 */
    // 用于设置属性
    public void setTotalGameNum(int num) {
//        statusTable.setMultipleValue(GameNum, num);
    }

    public void setCurGameNum(int num) {
        statusTable.setValue(GameNum, num);
    }

    public void setTotalPlayerNum(int num) {
//        MultipleStatusLabel label = (MultipleStatusLabel) controls.get(PlayerNum);
//        label.setLValue(num);
    }

    public void setRestPlayerNum(int num) {
        statusTable.setValue(PlayerNum, num);
    }
}

// 双列的的表示视窗
class TablePane extends GridPane {
    Vector<StatusType> types = new Vector<>();
    HashMap<StatusType, Pair<Label, NumberLabel>> labels = new HashMap<>();

    public TablePane() {
        this.setHgap(10);
    }

    // 添加状态 (只添加不删除)
    public void addStatus(StatusType type) {
        addStatus(type, 0);
    }

    // 添加状态 (只添加不删除)
    public void addStatus(StatusType type, int value) {
        if(checkExist(type)) {
            System.out.println("[info] Type exists");
            return;
        }

        Label nameLabel = new Label(type.getText());
        NumberLabel valueLabel = new NumberLabel(value);

        types.add(type);
        labels.put(type, new Pair<>(nameLabel, valueLabel));

        // 将Label添加到组件中
        this.addRow(this.getRowCount(), nameLabel, valueLabel);
    }

    public void addMultipleStatus(StatusType type, int totalValue) {
        if(checkExist(type)) {
            System.out.println("[info] Type exists");
            return;
        }

        Label nameLabel = new Label(type.getText());
        MultiNumberLabel valueLabel = new MultiNumberLabel(totalValue);

        types.add(type);
        labels.put(type, new Pair<>(nameLabel, valueLabel));

        // 将Label添加到组件中
        this.addRow(this.getRowCount(), nameLabel, valueLabel);
    }

    // 修改label中的值
    public void setValue(StatusType type, int value) {
        Pair<Label, NumberLabel> labelPair = labels.get(type);
        labelPair.getValue().setValue(value);
    }

    // 修改label中的值
    public void setMultipleValue(StatusType type, int value, int totalValue) {
        Pair<Label, NumberLabel> labelPair = labels.get(type);
        MultiNumberLabel label = (MultiNumberLabel) labelPair.getValue();
        label.setValue(value, totalValue);
    }

    public boolean checkExist(StatusType type) {
        return labels.containsKey(type);
    }
}

// 包含复合值的存放数字的Label Example: 3 / 5
class MultiNumberLabel extends NumberLabel {
    String divSymbol = " / ";

    int totalValue = -1;

    public MultiNumberLabel(int value, int totalValue) {
        this.setValue(value, totalValue);
    }

    public MultiNumberLabel(int totalValue) {
        this.setValue(totalValue, totalValue);
    }

    public void setValue(int value, int totalValue) {
        this.totalValue = totalValue;
        this.value = value;
        this.refresh();
    }

    void refresh() {
        super.setText(value + divSymbol + totalValue);
    }
}

// 数字Label
class NumberLabel extends Label {
    int value = -1;

    public NumberLabel(){}

    public NumberLabel(int value) {
        this.setValue(value);
    }

    public void setValue(int value) {
        this.value = value;
        this.refresh();
    }

    // 更改后刷新Label
    void refresh() {
        String text = value==-1 ? null : Integer.toString(value);
        super.setText(text);
    }

}

// 用于记录状态类型
enum StatusType {
    StatusTitle("游戏状态"),
    GameNum("局数"),
    PlayerNum("玩家数"),
    LastType("");

    final String text;

    StatusType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
