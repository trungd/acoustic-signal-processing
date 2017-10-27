package com.dvtrung.sound.train;

import com.dvtrung.sound.Options;
import com.dvtrung.sound.Utils;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import org.apache.commons.math3.util.DoubleArray;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.Scanner;

import static com.dvtrung.sound.Utils.cepstrum;
import static com.dvtrung.sound.Utils.featureScaling;

public class StatisticalModel {
    public static int FRAME_LENGTH_IN_MILIS = 25;
    private static StatisticalModel statisticalModel;

    public static StatisticalModel getInstance() {
        if (statisticalModel == null) statisticalModel = new StatisticalModel();
        return statisticalModel;
    }

    public static class TrainingResult {
        double[] mu;
        double[] sigma2;
        String label;

        public TrainingResult(String label) {
            this.label = label;
        }

        public void loadTraingResultFromFile() {
            Scanner in = null;
            try {
                in = new Scanner(new File("training_results/" + label + ".dat"));
                label = in.nextLine();
                int count = in.nextInt();
                mu = new double[count]; sigma2 = new double[count];
                for (int i = 0; i < count; i++) {
                    mu[i] = in.nextDouble();
                    sigma2[i] = in.nextDouble();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public double L(double[] wf) {
            double[] x = wf2x(wf);
            double s = 0;
            for (int d = 0; d < mu.length; d++) {
                s -= Math.pow(x[d] - mu[d], 2) / (2 * sigma2[d]);
            }
            return s;
        }

        public void save() {
            File f = new File("training_results/" + label + ".dat");
            try (FileWriter fw = new FileWriter(f, true)) {
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(label); bw.newLine();
                bw.write(String.valueOf(mu.length)); bw.newLine();
                for (int i = 0; i < mu.length; i++) {
                    bw.write(String.valueOf(mu[i]));
                    bw.newLine();
                    bw.write(String.valueOf(sigma2[i]));
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static TrainingResult train(String dir, String label) {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();

        double[][] waveform = new double[listOfFiles.length][];
        int sampleRate = 0;

        // Get all files in training folders
        for (int i = 0; i < listOfFiles.length; i++) {
            try (final AudioInputStream stream = AudioSystem.getAudioInputStream(listOfFiles[i])) {
                waveform[i] = Le4MusicUtils.readWaveformMonaural(stream);
                sampleRate = (int)stream.getFormat().getSampleRate();
                Options.getInstance().frameSize = (int)(Options.getInstance().sampleRate / 40);
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int frameSize = sampleRate * FRAME_LENGTH_IN_MILIS / 1000;; // d
        int frameCount = 0; // total frames in all files

        // For each file extract frame by frame
        for (double[] wf: waveform) frameCount += wf.length / frameSize;

        double[][] wf = new double[frameCount][];
        int c = 0;
        for (int i = 0; i < waveform.length; i++) {
            frameCount = waveform[i].length / frameSize;
            for (int j = 0; j < frameCount; j++) {
                wf[c] = new double[frameSize];
                for (int d = 0; d < frameSize; d++) wf[c][d] = waveform[i][j * frameSize + d];
                c++;
            }
        }

        double[][] x = new double[frameCount][];
        for (int i = 0; i < frameCount; i++) {
            x[i] = wf2x(wf[i]);
        }

        int size = x[0].length;

        TrainingResult r = new TrainingResult(label);
        r.mu = new double[size];
        r.sigma2 = new double[size];

        for (int d = 0; d < size; d++) {
            r.mu[d] = 0;
            for (int i = 0; i < frameCount; i++) r.mu[d] += x[i][d];
            r.mu[d] /= frameCount;

            r.sigma2[d] = 0;
            for (int i = 0; i < frameCount; i++) r.sigma2[d] += Math.pow(x[i][d] - r.mu[d], 2);
            r.sigma2[d] /= frameCount;
        }

        return r;
    }

    private static double[] wf2x(double[] wf) { return featureScaling(cepstrum(wf)); }
}
