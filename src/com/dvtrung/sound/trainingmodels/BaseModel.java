package com.dvtrung.sound.trainingmodels;

import com.dvtrung.sound.Options;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class BaseModel {
    public static double[][] extractFrames(String dir, int frameLengthInMilis) {
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

        int frameSize = sampleRate * frameLengthInMilis / 1000; // d
        int frameCount = 0; // total frames in all files

        // For each file extract frame by frame
        for (double[] wf: waveform) frameCount += wf.length / frameSize;

        double[][] wf = new double[frameCount][];
        int c = 0;
        for (int i = 0; i < waveform.length; i++) {
            int fc = waveform[i].length / frameSize;
            for (int j = 0; j < fc; j++) {
                wf[c] = new double[frameSize];
                for (int d = 0; d < frameSize; d++) wf[c][d] = waveform[i][j * frameSize + d];
                c++;
            }
        }

        return wf;
    }
}
