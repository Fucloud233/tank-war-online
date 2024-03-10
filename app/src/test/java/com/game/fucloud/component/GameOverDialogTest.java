package com.game.fucloud.component;

import com.tankWar.game.component.OverDialog;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;

public class GameOverDialogTest extends Application  {
    @Test
    public void testDialog() {
        Application.launch();
    }

    @Override
    public void start(Stage stage) throws Exception {


        // 用户名称
        String[] names = new String[]{
                "Tom"
                , "Jack"
                , "Alex"
        };

//        int[] scores = new int[]{
//                1, 2, 3
//        };

        int[] scores = new int[]{
                1
                , 2
                , 2
        };

        OverDialog dialog = new OverDialog(names, scores);

        dialog.display();





    }
}
