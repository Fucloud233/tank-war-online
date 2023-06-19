package com.tankWar.game.component.basic;

import com.tankWar.game.component.basic.NumberLabel;

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
        this.totalValue = totalValue;
        this.refresh();
    }

    public int getTotalValue() {
        return totalValue;
    }

    void refresh() {
        super.setText(value + divSymbol + totalValue);
    }
}
