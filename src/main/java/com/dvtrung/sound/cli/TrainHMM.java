package com.dvtrung.sound.cli;

import com.dvtrung.sound.lib.trainingmodels.HiddenMarkovModel;
import com.dvtrung.sound.lib.trainingmodels.StatisticalModel;

public final class TrainHMM {

    public static void main(String[] args) {
        HiddenMarkovModel hiddenMarkovModel = new HiddenMarkovModel(new String[] {"a", "i", "u", "e", "o"});
        hiddenMarkovModel.train();
        hiddenMarkovModel.save();
    }
}