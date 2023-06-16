package com.tankWar.game;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.util.Pair;

import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import static com.tankWar.game.StatusType.*;

public class GameStatusPane extends VBox {
    // 包含的组件信息
    Label statusTitle = new Label("游戏状态");
    StatusTable statusTable = new StatusTable();

    Label scoreTitle = new Label("计分板");
    ScoreTable scoreTable = new ScoreTable();

    public GameStatusPane() {
        init();
    }

    public GameStatusPane(String[] playerNames){
        init();

        for(String name: playerNames)
            this.scoreTable.addPlayer(name);
    }

    // 初始化上述组件信息
    void init() {
        // Status配置
        statusTable.addMultipleStatus(GameNum, 5);
        statusTable.addMultipleStatus(PlayerNum, 4);

        this.getChildren().addAll(statusTitle, new Separator(), statusTable);

        // Score配置
        this.getChildren().addAll(scoreTitle, new Separator(), scoreTable);

        // 设置样式
        URL styleURL = this.getClass().getResource("/css/label.css");
        if(styleURL != null)
            this.getStylesheets().add(styleURL.toExternalForm());

        // 设置Pane属性
        this.setPrefWidth(150);
        this.setPadding(new Insets(Config.MapPaddingSize));
        this.setSpacing(5);

        // 设置statusTitle
        statusTitle.setPadding(new Insets(5, 0, 0, 0));
        statusTitle.setStyle("-fx-font-style: BOLD");
        statusTitle.setStyle("-fx-font-size: 24px;");

        scoreTitle.setPadding(new Insets(5, 0, 0, 0));
        scoreTitle.setStyle("-fx-font-style: BOLD");
        scoreTitle.setStyle("-fx-font-size: 24px;");
    }

    /* 封装好的属性设置函数 */
    public void decRestPlayerNum() {
        int num = statusTable.getValue(PlayerNum);
        statusTable.setValue(PlayerNum, num - 1);
    }

    public void incPlayerScore(String playerName) {
        int num = scoreTable.getValue(playerName);
        scoreTable.setValue(playerName, num + 1);
    }

    /* 底层的属性设置函数 */
    // 用于设置属性
    public void setTotalGameNum(int num) {
        statusTable.setTotalValue(GameNum, num);
    }

    public void setCurGameNum(int num) {
        statusTable.setValue(GameNum, num);
    }

    public void setTotalPlayerNum(int num) {
        statusTable.setTotalValue(PlayerNum, num);
    }

    public void setRestPlayerNum(int num) {
        statusTable.setValue(PlayerNum, num);
    }
}

// 显示游戏状态的Pane
class StatusTable extends TablePane {
    // 用于映射状态与下标对应的而关系
    HashMap<StatusType, Integer> types = new HashMap<>();

    public StatusTable() {
        super(3);
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

        // 记录 (类型 - 对应组件下标) 映射关系
        types.put(type, labels.size());
        // 将组件添加文进状态中
        this.addRow(type.getText(), new NumberLabel(value));
    }

    public void addMultipleStatus(StatusType type, int totalValue) {
        if(checkExist(type)) {
            System.out.println("[info] Type exists");
            return;
        }

        // 记录 (类型 - 对应组件下标) 映射关系
        types.put(type, labels.size());
        addRow(type.getText(), new MultiNumberLabel(totalValue));
    }

    // 修改label中的值
    public void setValue(StatusType type, int value) {
        this.getValueLabel(type).setValue(value);
    }

    // 修改label中的值
    public void setMultipleValue(StatusType type, int value, int totalValue) {
        MultiNumberLabel label = (MultiNumberLabel) getValueLabel(type);
        label.setValue(value, totalValue);
    }

    // 修改label中的值
    public void setTotalValue(StatusType type, int totalValue) {
        MultiNumberLabel label = (MultiNumberLabel) getValueLabel(type);
        label.setTotalValue(totalValue);
    }

    public NumberLabel getValueLabel(StatusType type) {
        return super.getValueLabel(types.get(type));
    }

    public int getValue(StatusType type) {
        return getValueLabel(type).getValue();
    }

    public int getTotalValue(StatusType type) {
        MultiNumberLabel label = (MultiNumberLabel) getValueLabel(type);
        return label.getValue();
    }

    public boolean checkExist(StatusType type) {
        return types.containsKey(type);
    }
}

// 显示得分信息的Pane
class ScoreTable extends TablePane {
    HashMap<String, Integer> index = new HashMap<>();

    public ScoreTable() {
        this(null);
    }

    public ScoreTable(int playerNum) {
        super(playerNum);
    }

    public ScoreTable(String[] playerNames) {
        super(playerNames==null ? 0: playerNames.length);

        if(playerNames == null) {
            return;
        }

        for(String name: playerNames) {
            addPlayer(name);
        }
    }

    public void addPlayer(String name) {
        index.put(name, index.size());
        this.addRow(name, new NumberLabel());
    }

    public void setValue(String name, int value) {
        super.setValue(index.get(name), value);
    }

    public int getValue(String name) {
        return getValue(index.get(name));
    }
}

abstract class TablePane extends GridPane{
    Vector<Pair<Label, NumberLabel>> labels;

    public TablePane(int rowNum) {
        this.labels = new Vector<>(rowNum);

        this.setHgap(20);
        this.setVgap(5);
    }

    public void addRow(String name, NumberLabel valueLabel) {
        Label nameLabel = new Label(name);
        labels.add(new Pair<>(nameLabel, valueLabel));
        // 将Label添加到组件中
        this.addRow(labels.size(), nameLabel, valueLabel);
    }

    public void setValue(int index, int value) {
        Pair<Label, NumberLabel> labelPair = labels.get(index);
        labelPair.getValue().setValue(value);
    }

    public int getValue(int index) {
        return this.getValueLabel(index).getValue();
    }

    public NumberLabel getValueLabel(int index) {
        Pair<Label, NumberLabel> labelPair = labels.get(index);
        return labelPair.getValue();
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

    public void setTotalValue(int totalValue) {
        this.totalValue =totalValue;
        this.refresh();
    }

    public int getTotalValue() {
        return totalValue;
    }

    void refresh() {
        super.setText(value + divSymbol + totalValue);
    }
}

// 数字Label
class NumberLabel extends Label {
    int value;

    public NumberLabel(){
        this(0);
    }

    public NumberLabel(int value) {
        this.setValue(value);
    }

    public void setValue(int value) {
        this.value = value;
        this.refresh();
    }

    public int getValue() {
        return value;
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
