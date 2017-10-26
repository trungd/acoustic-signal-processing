package com.dvtrung.sound;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

public class Options {
    private static Options options;
    public int currentPos;
    public int frameSize;
    public double sampleRate = Le4MusicUtils.sampleRate;
    public boolean bEntirePeriod = true;
    public double[] waveform;

    public static Options getInstance() {
        if (options == null) {
            options = new Options();
            return options;
        } else return options;
    }

    public int getFrameCount() {
        return waveform.length / options.frameSize;
    }
}
