package com.dvtrung.sound.lib;

import com.dvtrung.sound.gui.Options;
import com.dvtrung.sound.gui.controllers.KaraokeController;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Arrays;
import java.util.stream.IntStream;

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
     * @return: fundamental frequency (Hz)
     */
    public static int getFundamentalFrequency(double[] waveform) {
        int length = Options.getInstance().frameSize;
        double[] ac = new double[length];
        Arrays.fill(ac, 0);
        int n = length;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length - i; j++) {
                ac[i] += waveform[j] * waveform[j + i];
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
     * Get the fundamental frequency at current frame position using SHS (subharmonic summation)
     * @param waveform
     * @return: fundamental frequency (Hz)
     */
    public static int getFundamentalFrequencySHS(double[] waveform) {
        double[] spec = getMagSpectrum(waveform);

        double[] W =
                Le4MusicUtils.hamming(spec.length);
                //Window.gaussian(0.4, spec.length);

        double[] P = IntStream.range(0, spec.length).mapToDouble(i -> spec[i] * W[i]).toArray();
        //double[] P = spec;
        int numSubharmonic = 15;

        double[] s = IntStream.range(0, spec.length).mapToDouble(i -> Math.log(i + 1) / Math.log(2)).toArray();

        double[][] subharmonics = new double[numSubharmonic][];
        subharmonics[0] = P;

        for (int i = 1; i < numSubharmonic; i++) {
            subharmonics[i] = new double[P.length];
            for (int k = 0; k < s.length; k++) {
                double s_ = s[k] + Math.log(i + 1) / Math.log(2);

                if (s_ > s[s.length - 1]) subharmonics[i][k] = 0;
                else {
                    // binary search to find index
                    int start = 0, end = s.length - 1;
                    while (start < end - 1) {
                        int mid = (start + end) / 2;
                        if (s_ < s[mid]) end = mid;
                        else start = mid;
                    }

                    subharmonics[i][k] = (P[end] - P[start]) / (s[end] - s[start]) * (s_ - s[start]) + P[start];
                }
            }
        }

        double[] sum = new double[spec.length];
        for (int i = 0; i < numSubharmonic; i++) {
            for (int k = 0; k < spec.length; k++) sum[k] += Math.pow(0.84, i) * subharmonics[i][k];
        }

        final int fftSize = 1 << Le4MusicUtils.nextPow2(waveform.length);
        int ret = Le4MusicUtils.argmax(sum);
        return (int) (ret * Options.getInstance().sampleRate / fftSize);
    }

    /**
     * Calculate cepstrum series from waveform
     * @param wf: waveform
     * @return cepstrum
     */
    public static double[] cepstrumCoefficient(double[] wf, int lifter) {
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

        return IntStream.range(0, lifter).mapToDouble(i -> cepstrum[i].abs()).toArray();

        //for (int i = CEPSTRUM_LIFTER; i < cepstrum.length; i++) cepstrum[i] = new Complex(0, 0);
        //for (int i = 0; i < CEPSTRUM_LIFTER; i++) cepstrum[i] = new Complex(0, 0);

        //double[] ret = Le4MusicUtils.irfft(cepstrum);
        //return ret;
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
    public static int getLoudness(double[] wf) {
        int length = wf.length;

        double sum = 0;
        for (int k = 0; k < length; k++) sum += Math.pow(wf[k], 2);

        return (int)(Math.sqrt(sum / length) * 1000);
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

    public static boolean isVoiced_ZeroCrossingRate(double[] wf) {
        int zcr = zeroCrossingRate(wf, 0);
        int ff = getFundamentalFrequency(Utils.getFrameWaveform(wf, 0));
        int range = ZERO_CROSSING_VOICED_SOUND_DIFFERENCE_RANGE;
        return !((zcr > 2 * ff + range) || (zcr < 2 * ff - range));
    }

    public static boolean isVoiced_Correlation(double[] wf, int freq) {
        int periodSize = (int) (1.0 / freq * KaraokeController.sampleRate);
        if (2 * periodSize > wf.length) return false;

        // calculate pcc (pearson correlation coefficient)
        int mid = wf.length / 2;
        double[] wf_smooth = wf; //Vector.medianSmoothing(wf, 5);
        double ppc = new PearsonsCorrelation().correlation(
                Arrays.copyOfRange(wf_smooth, mid - periodSize, mid - 1),
                Arrays.copyOfRange(wf_smooth, mid, mid + periodSize - 1));
        System.out.println(ppc);
        return Math.abs(ppc) > 0.52;
    }

    // Get waveform of the frame starting from current position
    public static double[] getFrameWaveform(double[] waveform) {
        return getFrameWaveform(waveform, (int)Options.getInstance().currentPos);
    }

    public static double[] getFrameWaveform(double[] waveform, int start) {
        int length = Math.min(Options.getInstance().frameSize, waveform.length - start);
        double[] wf = new double[length];
        System.arraycopy(waveform, start, wf, 0, length);
        return wf;
    }

    public static int f2noteId(double f) {
        return (int)(12 * Math.log10(f / 440) / Math.log10(2) + 69);
    }

    public static String f2note(double f) {
        int n = f2noteId(f);
        String[] notes = {
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, "A0", "A#0", "B0", "C1", "C#1", "D1", "D#1", "E1", "F1",
                "F#1", "G1", "G#1", "A1", "A#1", "B1", "C2", "C#2", "D2", "D#2",
                "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2", "C3", "C#3",
                "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
                "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4",
                "A#4", "B4", "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5", "G5",
                "G#5", "A5", "A#5", "B5", "C6", "C#6", "D6", "D#6", "E6", "F6",
                "F#6", "G6", "G#6", "A6", "A#6", "B6", "C7", "C#7", "D7", "D#7",
                "E7", "F7", "F#7", "G7", "G#7", "A7", "A#7", "B7", "C8"
        };
        if (n >= notes.length) return null; else return notes[n];
    }

    public static Complex[] getSpectrum(double[] wf) {
        final int fftSize = 1 << Le4MusicUtils.nextPow2(wf.length);
        final int fftSize2 = (fftSize >> 1) + 1;

        final double[] src = Arrays.stream(Arrays.copyOf(wf, fftSize))
                .map(w -> w / wf.length)
                .toArray();

        return Le4MusicUtils.rfft(src);
    }

    public static double[] getWaveformFromSpectrum(Complex[] spectrum) {
        return Le4MusicUtils.irfft(spectrum);
    }

    public static double[] getMagSpectrum(double[] wf) {
        return Arrays.stream(getSpectrum(wf))
                .mapToDouble(c -> c.abs())
                .toArray();
    }

    public static double[] getLogSpectrum(double[] wf) {
        return Arrays.stream(getSpectrum(wf))
                .mapToDouble(c -> Math.log10(c.abs()))
                .toArray();
    }
}
