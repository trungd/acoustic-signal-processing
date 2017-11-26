package com.dvtrung.sound.cli;

import com.dvtrung.sound.gui.Options;
import com.dvtrung.sound.lib.trainingmodels.HiddenMarkovModel;
import com.dvtrung.sound.lib.trainingmodels.StatisticalModel;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public final class TestHMM {

    public static void main(String[] args) {
        HiddenMarkovModel hiddenMarkovModel = new HiddenMarkovModel(new String[] {"a", "i", "u", "e", "o"});
        hiddenMarkovModel.load();

        try (final AudioInputStream stream = AudioSystem.getAudioInputStream(new File("training/hmm/e.wav"))) {
            double[] wf = Le4MusicUtils.readWaveformMonaural(stream);
            System.out.println(hiddenMarkovModel.getLabel(wf));
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}