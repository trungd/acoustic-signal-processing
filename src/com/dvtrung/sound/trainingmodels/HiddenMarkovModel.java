package com.dvtrung.sound.trainingmodels;

public class HiddenMarkovModel extends BaseModel {
    public static int FRAME_LENGTH_IN_MILIS = 50;
    public static String[] LABELS = new String[]{"a", "i", "u", "e", "o"};

    double[] pi;
    double[][] A;
    double[] phi;

    public void train() {
        //double[][] wf = extractFrames("training/" + trainingResults[c].label, FRAME_LENGTH_IN_MILIS);
    }
}
