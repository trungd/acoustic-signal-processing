package com.dvtrung.sound.lib;

import com.dvtrung.sound.gui.Options;

import java.util.Arrays;

public class MFCC {
    private final static int NUM_MEL_FILTERS = 26;
    private final static double LOWER_FILTER_FREQUENCY = 80.00;
    private final static double UPPER_FILTER_FREQUENCY = 8000.00;
    private final static int LIFTER = 13;

    public static double f2mel(double freq) {
        return 2595 * Math.log10(1 + freq / 700);
    }

    public static int[] fftBinIndices() {
        Options options = Options.getInstance();
        final int cBin[] = new int[NUM_MEL_FILTERS + 2];
        cBin[0] = (int) Math.round(LOWER_FILTER_FREQUENCY / options.sampleRate * options.frameSize);// cBin0
        cBin[cBin.length - 1] = (options.frameSize / 2);// cBin24
        for (int i = 1; i <= NUM_MEL_FILTERS; i++) {// from cBin1 to cBin23
            final double fc = centerFreq(i);// center freq for i th filter
            cBin[i] = (int) Math.round(fc / options.sampleRate * options.frameSize);
        }
        return cBin;
    }

    public static double[] melFilter(double bin[], int cBin[]) {
        final double temp[] = new double[NUM_MEL_FILTERS + 2];
        for (int k = 1; k <= NUM_MEL_FILTERS; k++) {
            double num1 = 0.0, num2 = 0.0;
            for (int i = cBin[k - 1]; i <= cBin[k]; i++) {
                num1 += ((i - cBin[k - 1] + 1) / (cBin[k] - cBin[k - 1] + 1)) * bin[i];
            }

            for (int i = cBin[k] + 1; i <= cBin[k + 1]; i++) {
                num2 += (1 - ((i - cBin[k]) / (cBin[k + 1] - cBin[k] + 1))) * bin[i];
            }

            temp[k] = num1 + num2;
        }
        final double fBank[] = new double[NUM_MEL_FILTERS];
        System.arraycopy(temp, 1, fBank, 0, NUM_MEL_FILTERS);
        return fBank;
    }

    public static double centerFreq(int i) {
        final double melFLow    = f2mel(LOWER_FILTER_FREQUENCY);
        final double melFHigh   = f2mel(UPPER_FILTER_FREQUENCY);
        final double temp       = melFLow + ((melFHigh - melFLow) / (NUM_MEL_FILTERS + 1)) * i;
        return inverseMel(temp);
    }

    public static double inverseMel(double x) {
        final double temp = Math.pow(10, x / 2595) - 1;
        return 700 * (temp);
    }

    public static double[] dct(double x[]) {
        final double cepc[] = new double[x.length];
        // perform DCT
        for (int n = 1; n <= x.length; n++) {
            for (int i = 1; i <= NUM_MEL_FILTERS; i++) {
                cepc[n - 1] += x[i - 1] * Math.cos(Math.PI * (n - 1) / NUM_MEL_FILTERS * (i - 0.5));
            }
        }
        return cepc;
    }

    public static double[] mfcc(double[] wf) {
        double[] magSpec = Utils.getMagSpectrum(wf);

        final int cBin[] = MFCC.fftBinIndices(); // same for all
        final double fBank[] = MFCC.melFilter(magSpec, cBin);
        final double f[] = Arrays.stream(fBank).map(d -> Math.log(d) < -50 ? -50 : Math.log(d)).toArray();

        double[] mfcc = MFCC.dct(f);
        return Arrays.copyOfRange(mfcc, 0, LIFTER);
    }
}
