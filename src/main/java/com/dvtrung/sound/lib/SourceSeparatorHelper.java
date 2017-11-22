package com.dvtrung.sound.lib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Arrays;

public class SourceSeparatorHelper {
    double[] waveform;
    int frameCount;
    RealMatrix W, H;
    double[][] _X, _P; // magnitude and phase of spectrum
    int numSource;

    public static class NMFResult {
        public RealMatrix W, H;
        public NMFResult(RealMatrix W, RealMatrix H) {
            this.W = W; this.H = H;
        }
    }

    public NMFResult NMF(RealMatrix A, int k) {
        RealMatrix W = MathUtils.createRandomMatrix(A.getRowDimension(), k);
        RealMatrix H = MathUtils.createRandomMatrix(k, A.getColumnDimension());

        RealMatrix prevW = MatrixUtils.createRealMatrix(A.getRowDimension(), k);
        RealMatrix prevH = MatrixUtils.createRealMatrix(k, A.getColumnDimension());

        double e = 0.00000001;
        while ((W.subtract(prevW).getFrobeniusNorm() > e) || (H.subtract(prevH).getFrobeniusNorm() > e)) {
            prevH = H.copy();
            prevW = W.copy();

            RealMatrix A1 = W.transpose().multiply(A);
            RealMatrix B1 = W.transpose().multiply(W).multiply(H);
            for (int i = 0; i < H.getRowDimension(); i++) for (int j = 0; j < H.getColumnDimension(); j++)
                H.setEntry(i, j, H.getEntry(i, j) * (A1.getEntry(i, j) / B1.getEntry(i, j)));

            RealMatrix A2 = A.multiply(H.transpose());
            RealMatrix B2 = W.multiply(H).multiply(H.transpose());
            for (int i = 0; i < W.getRowDimension(); i++) for (int j = 0; j < W.getColumnDimension(); j++)
                W.setEntry(i, j, W.getEntry(i, j) * (A2.getEntry(i, j) / B2.getEntry(i, j)));
        }

        return new NMFResult(W, H);
    }

    public SourceSeparatorHelper(int numSource) {
        this.numSource = numSource;
    }

    public void separate(double[] waveform, int frameSize) {
        this.waveform = waveform;
        frameCount = waveform.length / frameSize;

        double[][] WF = MathUtils.reshape(waveform, frameSize);
        _X = new double[WF.length][];
        _P = new double[WF.length][];
        Complex[][] spectrum = new Complex[WF.length][];

        for (int i = 0; i < WF.length; i++) {
            spectrum[i] = Utils.getSpectrum(WF[i]);
            _X[i] = Arrays.stream(spectrum[i]).mapToDouble(c -> c.abs()).toArray();
            _P[i] = Arrays.stream(spectrum[i]).mapToDouble(c -> c.getArgument()).toArray();
        }
        RealMatrix X = MatrixUtils.createRealMatrix(_X);

        NMFResult r = NMF(X, numSource);
        W = r.W; H = r.H;
    }

    public double[] getSource(int source) {
        RealMatrix Xs = W.getColumnMatrix(source).multiply(H.getRowMatrix(source));

        double[] wfRes = new double[waveform.length];
        for (int i = 0; i < frameCount; i++) {
            Complex[] spec = new Complex[Xs.getRow(i).length];
            for (int j = 0; j < spec.length; j++)
                spec[j] = new Complex(Xs.getEntry(i, j) * Math.cos(_P[i][j]), Xs.getEntry(i, j) * Math.sin(_P[i][j]));

            double[] wf = Utils.getWaveformFromSpectrum(spec);
            for (int k = 0; k < spec.length; k++)
                wfRes[i * spec.length + k] = wf[k];
        }

        return wfRes;
    }
}
