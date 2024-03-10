package com.tankWar.client.game.component.basic;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

public class TitleLabel extends VBox {
    Label titleLabel = new Label();

    public TitleLabel(){
        this(null);
    }

    public TitleLabel(String text) {
        titleLabel.setText(text);
        this.init();
    }

    void init() {
        this.getChildren().addAll(titleLabel, new Separator());

        titleLabel.setStyle("-fx-font-style: BOLD");
        titleLabel.setStyle("-fx-font-size: 24px;");

        this.setSpacing(5);
        this.setPadding(new Insets(5, 0, 0, 0));
    }

    public void setText(String text) {
        this.titleLabel.setText(text);
    }
}
