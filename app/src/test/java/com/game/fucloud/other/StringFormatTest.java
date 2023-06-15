package com.game.fucloud.other;

import org.junit.Test;

public class StringFormatTest {
    @Test
    public void stringTest() {
        int num = 0;
        String text = String.format("%03d", num);
        System.out.println(text);

        for(int i=0; i<=100; i++) {
            String temp_text = transferNum(i);
            int temp_num = transferStr(temp_text);

            if(i != temp_num) {
                System.out.println("[Error] " + i + " " + temp_text + " " + temp_num);
                return;
            }
        }

        System.out.println("[info] Success!");
    }

    String transferNum(int num) {
        return String.format("%03d", num);
    }

    int transferStr(String str) {
        return Integer.parseInt(str);
    }
}
