package com.tankWar.game.component.basic;

import javafx.scene.control.Label;

// 数字Label
class NumberLabel extends Label {
    int value;

    public NumberLabel() {
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
        String text = value == -1 ? null : Integer.toString(value);
        super.setText(text);
    }
}
