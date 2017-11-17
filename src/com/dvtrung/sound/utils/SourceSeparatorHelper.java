package com.dvtrung.sound.utils;

import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.math3.complex.Complex;
import sun.jvm.hotspot.utilities.IntArray;

import java.util.Arrays;
import java.util.stream.IntStream;

public class SourceSeparatorHelper {
    double[] waveform;
    int frameCount;
    Matrix W, H;
    double[][] _X, _P; // magnitude and phase of spectrum
    int numSource;

    public SourceSeparatorHelper(int numSource) {
        this.numSource = numSource;
    }

    public void separate(double[] waveform, int frameSize) {
        this.waveform = waveform;
        frameCount = waveform.length / frameSize;

        double[][] WF = Matrix.reshapeArr(waveform, frameSize).e;
        _X = new double[WF.length][];
        _P = new double[WF.length][];
        Complex[][] spectrum = new Complex[WF.length][];

        for (int i = 0; i < WF.length; i++) {
            spectrum[i] = Utils.getSpectrum(WF[i]);
            _X[i] = Arrays.stream(spectrum[i]).mapToDouble(c -> c.abs()).toArray();
            _P[i] = Arrays.stream(spectrum[i]).mapToDouble(c -> c.getArgument()).toArray();
        }
        Matrix X = new Matrix(_X);

        Matrix.NMFResult r = X.NMF(numSource);
        W = r.W; H = r.H;
    }

    public double[] getSource(int source) {
        Matrix Xs = W.col(source).mul(H.row(source));

        double[] wfRes = new double[waveform.length];
        for (int i = 0; i < frameCount; i++) {
            Complex[] spec = new Complex[Xs.e[i].length];
            for (int j = 0; j < spec.length; j++)
                spec[j] = new Complex(Xs.e[i][j] * Math.cos(_P[i][j]), Xs.e[i][j] * Math.sin(_P[i][j]));

            double[] wf = Utils.getWaveformFromSpectrum(spec);
            for (int k = 0; k < spec.length; k++)
                wfRes[i * spec.length + k] = wf[k];
        }

        return wfRes;
    }
}
