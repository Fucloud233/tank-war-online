package com.tankWar.game.component.basic;

import java.util.HashMap;

// 显示游戏状态的Pane
public class StatusTable extends ValueTable {
    // 用于映射状态与下标对应的而关系
    HashMap<StatusType, Integer> types = new HashMap<>();

    public StatusTable(String title) {
        super(title);
    }

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
