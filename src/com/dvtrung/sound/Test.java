package com.dvtrung.sound;

import com.dvtrung.sound.utils.SourceSeparatorHelper;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.WavWriter;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public final class Test {
    public static void main(String[] args) {
        //Matrix A = new Matrix(new double[][]{{1,2,3}, {4,5,6}, {7,8,9}});
        //Matrix.NMFResult r = A.NMF(1);
        //r.M.mul(r.N).print();

        File file = new File("test.wav");
        double[] waveform;
        double sampleRate;
        int frameSize;
        try (final AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
            waveform = Le4MusicUtils.readWaveformMonaural(stream);
            sampleRate = stream.getFormat().getSampleRate();
            frameSize = (int)(sampleRate / 50); // 20ms
        } catch (IOException e) {
            Le4MusicUtils.showAlertAndWait("I/O Error: " + file, e);
            return;
        } catch (UnsupportedAudioFileException e) {
            Le4MusicUtils.showAlertAndWait("Unsupported Audio File: " + file, e);
            return;
        }

        SourceSeparatorHelper helper = new SourceSeparatorHelper(2);
        helper.separate(waveform, frameSize);
        double[] wf = helper.getSource(0);

        try {
            WavWriter wavWriter = WavWriter.newWavWriter(new File("ss.wav"), sampleRate);
            wavWriter.append(wf);
            wavWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}