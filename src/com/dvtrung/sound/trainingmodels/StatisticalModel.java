package com.dvtrung.sound.trainingmodels;

import com.dvtrung.sound.utils.MFCC;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import static com.dvtrung.sound.utils.Utils.cepstrumCoefficient;

public class StatisticalModel extends BaseModel {
    public static int FRAME_LENGTH_IN_MILIS = 50;
    private static StatisticalModel statisticalModel;
    private static TrainingResult[] trainingResults;

    public StatisticalModel(String[] labels) {
        trainingResults = new TrainingResult[labels.length];
        for (int i = 0; i < labels.length; i++) {
            trainingResults[i] = new TrainingResult(labels[i]);
        }
    }

    public void load() {
        for (int i = 0; i < trainingResults.length; i++) {
            trainingResults[i].loadTrainingResultFromFile();
        }
    }

    public class TrainingResult {
        double[] mu;
        double[] sigma;
        String label;

        public TrainingResult(String label) {
            this.label = label;
        }

        public void loadTrainingResultFromFile() {
            Scanner in = null;
            try {
                in = new Scanner(new File("training_results/" + label + ".dat"));
                label = in.nextLine();
                int count = in.nextInt();
                mu = new double[count]; sigma = new double[count];
                for (int i = 0; i < count; i++) {
                    mu[i] = in.nextDouble();
                    sigma[i] = in.nextDouble();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public double L(double[] wf) {
            double[] x = getFeatureVector(wf);
            double s = 0;
            for (int d = 0; d < mu.length; d++) {
                s -= Math.log(sigma[d]) + Math.pow(x[d] - mu[d], 2) / (2 * sigma[d] * sigma[d]);
            }
            return s;
        }

        public void save() {
            File f = new File("training_results/" + label + ".dat");
            try (FileWriter fw = new FileWriter(f)) {
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(label); bw.newLine();
                bw.write(String.valueOf(mu.length)); bw.newLine();
                for (int i = 0; i < mu.length; i++) {
                    bw.write(String.valueOf(mu[i]));
                    bw.newLine();
                    bw.write(String.valueOf(sigma[i]));
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public double[] getFeatureVector(double[] wf) {
            //return /*featureScaling*/(cepstrum(wf));
            //return MFCC.mfcc(wf);
            return cepstrumCoefficient(wf, 13);
        }
    }

    public void train() {
        for (int c = 0; c < trainingResults.length; c++) {
            double[][] wf = extractFrames("training/" + trainingResults[c].label, FRAME_LENGTH_IN_MILIS);
            TrainingResult r = trainingResults[c];

            int frameCount = wf.length;
            double[][] x = new double[frameCount][];
            for (int i = 0; i < frameCount; i++) {
                x[i] = r.getFeatureVector(wf[i]);
            }

            int size = x[0].length;

            r.mu = new double[size];
            r.sigma = new double[size];

            for (int d = 0; d < size; d++) {
                r.mu[d] = 0;
                for (int i = 0; i < frameCount; i++) r.mu[d] += x[i][d];
                r.mu[d] /= frameCount;

                r.sigma[d] = 0;
                for (int i = 0; i < frameCount; i++) r.sigma[d] += Math.pow(x[i][d] - r.mu[d], 2);
                r.sigma[d] = Math.sqrt(r.sigma[d] / frameCount);
            }
        }
    }

    public String getLabel(double[] wf) {
        double[] loss = new double[5];
        int ret = 0;
        for (int k = 0; k < 5; k++) {
            loss[k] = trainingResults[k].L(wf);
        }
        return trainingResults[Le4MusicUtils.argmax(loss)].label;
    }

    public void save() {
        for (TrainingResult trainingResult: trainingResults) trainingResult.save();
    }
}
