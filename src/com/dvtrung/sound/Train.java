package com.dvtrung.sound;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class Train {
    private static int FRAME_LENGTH_MILI = 25;

    public static class TrainingResult {
        double[] mu;
        double[] sigma;

        public double L(double[] x) {
            double s = 0;
            for (int d = 0; d < mu.length; d++) {
                s -= Math.pow(x[d] - mu[d], 2) / (2 * Math.pow(sigma[d], 2));
            }
            return s;
        }

    }

    public static TrainingResult train(String filePath) {
        double[] waveform = new double[0];
        int sampleRate = 0;
        int frameSize; // d
        int frameCount; // N

        try (final AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filePath))) {
            waveform = Le4MusicUtils.readWaveformMonaural(stream);
            sampleRate = (int)stream.getFormat().getSampleRate();
            Options.getInstance().frameSize = (int)(Options.getInstance().sampleRate / 40);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        frameSize = sampleRate / (1000 / FRAME_LENGTH_MILI);
        frameCount = waveform.length / frameSize;

        double[][] wf = new double[frameCount][frameSize];
        for (int i = 0; i < frameCount; i++) {
            for (int d = 0; d < frameSize; d++) wf[i][d] = waveform[i * frameSize + d];
        }

        double[][] x = new double[frameCount][];
        for (int i = 0; i < frameCount; i++) {
            x[i] = Utils.cepstrum(wf[i]);
        }

        int size = x[0].length;

        TrainingResult r = new TrainingResult();
        r.mu = new double[size];
        r.sigma = new double[size];

        for (int d = 0; d < size; d++) {
            r.mu[d] = 0;
            for (int i = 0; i < frameCount; i++) r.mu[d] += x[i][d];
            r.mu[d] /= frameCount;

            r.sigma[d] = 0;
            for (int i = 0; i < frameCount; i++) r.sigma[d] += Math.pow(x[i][d] - r.mu[d], 2);
            r.sigma[d] /= frameCount;

            System.out.println(String.valueOf(r.mu[d]) + " " + String.valueOf(r.sigma[d]));
        }

        return r;
    }
}
