package com.dvtrung.sound.lib.trainingmodels;

import com.dvtrung.sound.gui.Options;
import com.dvtrung.sound.lib.Utils;

import java.util.Arrays;

/**
 * Helper class for training and getting result
 */
public class TrainingHelper {
    private static TrainingHelper trainingHelper;
    StatisticalModel statisticalModel;

    public static TrainingHelper getInstance() {
        if (trainingHelper == null) trainingHelper = new TrainingHelper();
        return trainingHelper;
    }

    public TrainingHelper() {
        statisticalModel = new StatisticalModel(new String[] { "a", "i", "u", "e", "o" });
        statisticalModel.load();
    }

    public String getLabel(double[] waveform, int from) {
        int frameSize = StatisticalModel.FRAME_LENGTH_IN_MILIS * (int) Options.getInstance().sampleRate / 1000;
        double[] wf = Arrays.copyOfRange(waveform, from, from + frameSize);
        return statisticalModel.getLabel(wf);
    }
}
