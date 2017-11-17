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
    public double sampleRate = Le4MusicUtils.sampleRate;
    public int frameSize = (int)(Le4MusicUtils.frameInterval * Le4MusicUtils.sampleRate);
    public double[] waveform;
    // is recording now
    public boolean recording = false;

    public static Options getInstance() {
        if (options == null) {
            options = new Options();
            return options;
        } else return options;
    }

    public static void newInstance() {
        options = null;
    }

    public int getFrameCount() {
        return waveform.length / options.frameSize;
    }

    public int getFrameDurationInMilis() {
        return 1000 * frameSize / (int)sampleRate;
    }

    public double getFrameDuration() {
        return frameSize / sampleRate;
    }

    public void setFrameDurationInMilis(int duration) {
        frameSize = duration * (int)sampleRate / 1000;
    }

    public double getCurrentTime() { return currentPos / sampleRate; }

    public void setRecording(boolean r) { recording = r; }
}
