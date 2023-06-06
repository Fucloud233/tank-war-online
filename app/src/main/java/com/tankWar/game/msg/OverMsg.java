package com.tankWar.game.msg;

public class OverMsg extends Message{
    int[] scores = null;

    public OverMsg() {
        super(-1, MessageType.Over);
    }

    public OverMsg(int[] scores) {
        super(-1, MessageType.Over);
        this.scores = scores;
    }

    public int[] getScores() {
        return scores;
    }
}
