package com.dvtrung.sound;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

/**
 * Helper class for storing application state and configuration
 */
public class Options {
    private static Options options;
    // Current frame's position
    public int currentPos;
    // Size of a frame
    public int frameSize;
    public double sampleRate = Le4MusicUtils.sampleRate;
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

    public int getFrameDurationInMilis() {
        return 1000 * frameSize / (int)sampleRate;
    }

    public void setFrameDurationInMilis(int duration) {
        frameSize = duration * (int)sampleRate / 1000;
    }

    public double getCurrentTime() { return currentPos / sampleRate; }
}
