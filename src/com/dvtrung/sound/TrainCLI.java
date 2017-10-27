package com.dvtrung.sound;

import com.dvtrung.sound.train.StatisticalModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public final class TrainCLI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String[] args = getParameters().getRaw().toArray(new String[0]);

        StatisticalModel.train("training/a", "a").save();
        StatisticalModel.train("training/i", "i").save();
        StatisticalModel.train("training/u", "u").save();
        StatisticalModel.train("training/e", "e").save();
        StatisticalModel.train("training/o", "o").save();

        Platform.exit();
    }
}