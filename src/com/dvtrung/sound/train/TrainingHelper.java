package com.dvtrung.sound.train;

import com.dvtrung.sound.Options;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import java.util.Arrays;

/**
 * Helper class for training and getting result
 */
public class TrainingHelper {
    private static TrainingHelper trainingHelper;
    StatisticalModel.TrainingResult[] trainingResults = new StatisticalModel.TrainingResult[5];

    public static TrainingHelper getInstance() {
        if (trainingHelper == null) trainingHelper = new TrainingHelper();
        return trainingHelper;
    }

    public TrainingHelper() {
        trainingResults[0] = new StatisticalModel.TrainingResult("a");
        trainingResults[1] = new StatisticalModel.TrainingResult("i");
        trainingResults[2] = new StatisticalModel.TrainingResult("u");
        trainingResults[3] = new StatisticalModel.TrainingResult("e");
        trainingResults[4] = new StatisticalModel.TrainingResult("o");
        for (StatisticalModel.TrainingResult result: trainingResults) result.loadTraingResultFromFile();
    }

    public int getLabelIndex(double[] waveform, int from) {
        int frameSize = StatisticalModel.FRAME_LENGTH_IN_MILIS * (int) Options.getInstance().sampleRate / 1000;
        double[] loss = new double[5];
        int ret = 0;
        for (int k = 0; k < 5; k++) {
            double[] wf = Arrays.copyOfRange(waveform, from, from + frameSize);
            loss[k] = trainingResults[k].L(wf);
        }
        return Le4MusicUtils.argmax(loss);
    }

    public String getLabel(double[] wf, int from) {
        int id = getLabelIndex(wf, from);
        switch (id) {
            case 0: return "あ";
            case 1: return "い";
            case 2: return "う";
            case 3: return "え";
            case 4: return "お";
            default: return null;
        }
    }
}
