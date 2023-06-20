package com.tankWar.game.component.basic;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.Vector;

public abstract class ValueTable extends VBox {
    Vector<Pair<Label, NumberLabel>> labels;

    TitleLabel titleLabel = new TitleLabel();
    GridPane mainGrid = new GridPane();

    public ValueTable(String title, int rowNum) {
        // 设置标题
        titleLabel.setText(title);

        // 设置表格
        this.mainGrid.setHgap(20);
        this.mainGrid.setVgap(5);

        this.labels = new Vector<>(rowNum);

        this.setSpacing(5);

        this.getChildren().addAll(titleLabel, mainGrid);
    }

    public ValueTable(int rowNum) {
        this("标题", rowNum);
    }

    public ValueTable(String title) {
        this(title, 4);
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    public void addRow(String name, NumberLabel valueLabel) {
        Label nameLabel = new Label(name);
        labels.add(new Pair<>(nameLabel, valueLabel));
        // 将Label添加到组件中
        mainGrid.addRow(labels.size(), nameLabel, valueLabel);
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
