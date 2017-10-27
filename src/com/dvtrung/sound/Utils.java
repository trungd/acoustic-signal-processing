package com.dvtrung.sound;

import javafx.scene.chart.XYChart;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import org.apache.commons.math3.complex.Complex;

import java.util.Arrays;
import java.util.Collections;

/**
 * Miscellaneous function
 */
public class Utils {

    // Number of coefficients remains before taking inverse Fourier transform when finding cepstrum
    public static int CEPSTRUM_LIFTER = 13;

    // Second peak condition: highest value in a range to the left and to the right
    public static int AUTOCORRELATION_PEAK_RANGE = 20;

    // Acceptable difference of 2 * zero crossing (in a second) and frequency
    public static int ZERO_CROSSING_VOICED_SOUND_DIFFERENCE_RANGE = 500;

    /**
     * Get the fundamental frequency at current frame position
     * @param waveform
     * @param from: starting position
     * @return: fundamental frequency (Hz)
     */
    public static int getFundamentalFrequency(double[] waveform, int from) {
        int length = Options.getInstance().frameSize;
        double[] ac = new double[length];
        Arrays.fill(ac, 0);
        int n = length;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length - i; j++) {
                ac[i] += waveform[j + from] * waveform[from + j + i];
            }
        }

        for (int i = AUTOCORRELATION_PEAK_RANGE; i < ac.length - AUTOCORRELATION_PEAK_RANGE; i++) {
            boolean flag = false;
            for (int j = i - AUTOCORRELATION_PEAK_RANGE; j < i + AUTOCORRELATION_PEAK_RANGE; j++) if (ac[j] > ac[i]) {
                flag = true;
                break;
            }
            if (!flag) return (int)(Options.getInstance().sampleRate / i);
        }

        return 0;
    }

    /**
     * Calculate cepstrum series from waveform
     * @param wf: waveform
     * @return cepstrum
     */
    public static double[] cepstrum(double[] wf) {
        final int fftSize = 1 << Le4MusicUtils.nextPow2(wf.length);
        final int fftSize2 = (fftSize >> 1) + 1;

        final double[] src = Arrays.stream(Arrays.copyOf(wf, fftSize))
                .map(w -> w / wf.length)
                .toArray();

        final Complex[] spectrum = Le4MusicUtils.rfft(src);

        double[] specLog = Arrays.stream(spectrum)
                .mapToDouble(c -> Math.log10(c.abs()))
                .toArray();

        Complex[] cepstrum = Le4MusicUtils.rfft(Arrays.copyOf(specLog, specLog.length - 1));

        //for (int i = CEPSTRUM_LIFTER; i < cepstrum.length; i++) cepstrum[i] = new Complex(0, 0);
        for (int i = 0; i < CEPSTRUM_LIFTER; i++) cepstrum[i] = new Complex(0, 0);

        double[] ret = Le4MusicUtils.irfft(cepstrum);
        return ret;
    }

    /**
     * Calculate zero crossing number
     * @param waveform
     * @param from: starting position (length: frame size
     * @return number of zero crossing
     */
    public static int zeroCrossingRate(double[] waveform, int from) {
        int length = Options.getInstance().frameSize;
        int count = 0;
        for (int i = 1; i < length; i++) if (waveform[i + from] * waveform[i + from - 1] < 0) count += 1;
        return count;
    }

    /**
     * Get loudness of current frame
     * @return loudness (dB)
     */
    public static int getLoudness() {
        int start = Options.getInstance().currentPos;
        int length = Options.getInstance().frameSize;
        double[] waveform = Options.getInstance().waveform;

        double sum = 0;
        for (int k = start; k <= start + length; k++) sum += Math.pow(waveform[k], 2);

        return (int)(20 * Math.log10(sum / length));
    }

    /**
     * Normalize a vector
     * @param x: vector
     * @return normalized vector
     */
    public static double[] featureScaling(double[] x) {
        double[] y = new double[x.length];
        double min = x[0]; double max = x[0];
        for (double t: x) {
            if (t < min) min = t;
            if (t > max) max = t;
        }
        for (int i = 0; i < y.length; i++) y[i] = (x[i] - min) / (max - min);
        return y;
    }
}
