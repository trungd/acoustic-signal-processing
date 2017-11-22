package com.dvtrung.sound.cli;

import com.dvtrung.sound.lib.trainingmodels.StatisticalModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public final class TrainCLI {

    public static void main(String[] args) {
        StatisticalModel statisticalModel = new StatisticalModel(new String[] {"a", "i", "u", "e", "o"});
        statisticalModel.train();
        statisticalModel.save();
    }
}