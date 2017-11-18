package com.dvtrung.sound.cli;

import com.dvtrung.sound.lib.trainingmodels.StatisticalModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public final class TrainCLI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String[] args = getParameters().getRaw().toArray(new String[0]);

        StatisticalModel statisticalModel = new StatisticalModel(new String[] {"a", "i", "u", "e", "o"});
        statisticalModel.train();
        statisticalModel.save();

        Platform.exit();
    }
}