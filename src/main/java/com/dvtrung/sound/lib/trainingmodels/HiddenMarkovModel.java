package com.dvtrung.sound.lib.trainingmodels;

import com.dvtrung.sound.lib.MathUtils;
import com.dvtrung.sound.lib.Utils;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.*;
import java.util.Scanner;

import static com.sun.tools.doclint.Entity.mu;

public class HiddenMarkovModel extends BaseModel {
    public static int FRAME_LENGTH_IN_MILIS = 50;
    public static int NUM_COEFFICIENT = 13;
    public int sampleRate = 16000;

    private static TrainingResult[] trainingResults;
    public String[] labels;

    public HiddenMarkovModel(String[] labels) {
        this.labels = labels;
        trainingResults = new TrainingResult[labels.length];
        for (int i = 0; i < labels.length; i++) {
            trainingResults[i] = new TrainingResult(labels[i]);
        }
    }

    public void load() {
        for (int i = 0; i < trainingResults.length; i++) {
            trainingResults[i].loadTrainingResultFromFile();
        }
    }

    class TrainingResult {
        int m; // number of observations
        int n; // number of states
        int K; // number of dimensions of observation
        double[][] A;
        double[][] B_mu;
        double[][][] B_sigma;
        double[] pi;
        double P;
        String label;

        int T;
        double[][] O;
        double[][] alpha, beta, gamma;

        TrainingResult(String label) {
            this.label = label;
        }

        public void init(int m, int n, int K) {
            this.m = m;
            this.n = n;
            this.K = K;

            A = MatrixUtils.createRealIdentityMatrix(n).getData();
            B_mu = MathUtils.createRandomMatrix(n, K).getData();
            //B_mu = new double[n][K];
            B_sigma = new double[n][][];
            for (int i = 0; i < n; i++)
                B_sigma[i] = MatrixUtils.createRealIdentityMatrix(K).getData();
            pi = new double[n];
        }

        public void loadTrainingResultFromFile() {
            try {
                FileInputStream f = new FileInputStream("training_results/hmm/" + label + ".dat");
                ObjectInputStream iis = new ObjectInputStream(f);
                m = iis.readInt(); n = iis.readInt(); K = iis.readInt();
                A = (double[][])iis.readObject();
                B_mu = (double[][])iis.readObject();
                B_sigma = (double[][][])iis.readObject();
                pi = (double[])iis.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void save() {
            FileOutputStream f = null;
            try {
                f = new FileOutputStream("training_results/hmm/" + label + ".dat");
                ObjectOutputStream oos = new ObjectOutputStream(f);
                oos.writeInt(m); oos.writeInt(n); oos.writeInt(K);
                oos.writeObject(A);
                oos.writeObject(B_mu); oos.writeObject(B_sigma);
                oos.writeObject(pi);
                f.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public double calB(int i, double[] obs) {
            return new MultivariateNormalDistribution(B_mu[i], B_sigma[i]).density(obs);
        }

        public void forward() {
            //for (int i = 0; i < n; i++) alpha[0][i] = pi[i] * calB(i, O[0]);
            alpha[0][0] = 1;
            for (int i = 1; i < n; i++) alpha[0][i] = pi[i] * calB(i, O[0]);

            for (int t = 1; t < T; t++) {
                for (int i = 0; i < n; i++) {
                    alpha[t][i] = 0;
                    for (int j = 0; j < n; j++) {
                        alpha[t][i] += alpha[t - 1][j] * A[j][i];
                    }
                    alpha[t][i] *= calB(i, O[t]);
                }
            }

            P = 0;
            for (int i = 0; i < n; i++) P += alpha[T - 1][i];
        }

        public double getProb(double[][] obs) {
            O = obs;
            T = obs.length;
            forward();
            return P;
        }

        public void backward() {
            for (int i = 0; i < n; i++) beta[T - 1][i] = 1;
            for (int t = T - 2; t >= 0; t--) {
                for (int i = 0; i < n; i++) {
                    beta[t][i] = 0;
                    for (int j = 0; j < n; j++) {
                        beta[t][i] += A[i][j] * calB(j, O[t + 1]) * beta[t + 1][j];
                    }
                }
            }

            gamma = new double[T][n];
            for (int t = 0; t < T; t++) for (int i = 0; i < n; i++) gamma[t][i] = alpha[t][i] * beta[t][i] / P;
        }

        public void train(double[][] obs) {
            O = obs;
            T = obs.length;
            alpha = new double[T][n];
            beta = new double[T][n];
            pi[0] = 1;

            for (int c = 0; c < 1000; c++) {
                forward();
                backward();

                double[][][] digamma = new double[T][n][n];
                for (int t = 0; t < T - 1; t++)
                    for (int i = 0; i < n; i++)
                        for (int j = 0; j < n; j++)
                            digamma[t][i][j] = alpha[t][i] * A[i][j] * calB(j, O[t + 1]) * beta[t + 1][j] / P;

                for (int i = 0; i < n; i++) pi[i] = gamma[0][i];
                for (int i = 0; i < n; i++) {
                    double Y = 0;
                    for (int t = 0; t < T - 1; t++) Y += gamma[t][i];
                    for (int j = 0; j < n; j++) {
                        double X = 0;
                        for (int t = 0; t < T - 1; t++) X += digamma[t][i][j];
                        A[i][j] = X / Y;
                    }
                }

                double[] Y = new double[n];
                for (int j = 0; j < n; j++) {
                    Y[j] = 0;
                    for (int t = 0; t < T - 1; t++) Y[j] += gamma[t][j];
                    for (int k = 0; k < K; k++) {
                        B_mu[j][k] = 0;
                        for (int t = 0; t < T; t++) B_mu[j][k] += gamma[t][j] * O[t][k];
                        B_mu[j][k] /= Y[j];
                    }
                }

                for (int j = 0; j < n; j++) {
                    for (int k1 = 0; k1 < K; k1++) {
                        for (int k2 = 0; k2 < K; k2++) {
                            B_sigma[j][k1][k2] = 0;
                            for (int t = 0; t < T - 1; t++) B_sigma[j][k1][k2] += gamma[t][j] * (O[t][k1] - B_mu[j][k1]) * (O[t][k2] - B_mu[j][k2]);
                            B_sigma[j][k1][k2] /= Y[j];
                        }
                    }
                }
            }
        }
    }

    private int getFrameSize() {
        return (int) (FRAME_LENGTH_IN_MILIS / 1000.0 * sampleRate);
    }

    public String getLabel(double[] wf) {
        double[] loss = new double[labels.length];
        int ret = 0;

        double[][] _wf = MathUtils.reshape(wf, getFrameSize());

        double x[][] = new double[_wf.length][];
        for (int i = 0; i < _wf.length; i++) x[i] = getFeatureVector(_wf[i]);

        for (int k = 0; k < labels.length; k++) {
            loss[k] = trainingResults[k].getProb(x);
        }
        return trainingResults[Le4MusicUtils.argmax(loss)].label;
    }

    public void train() {
        for (int c = 0; c < trainingResults.length; c++) {
            double[][] wf = extractFramesFromFile("training/hmm/" + trainingResults[c].label + ".wav", FRAME_LENGTH_IN_MILIS);
            TrainingResult r = trainingResults[c];

            double x[][] = new double[wf.length][];
            for (int i = 0; i < wf.length; i++) x[i] = getFeatureVector(wf[i]);

            r.init(x.length, 3, NUM_COEFFICIENT);
            r.train(x);
        }
    }

    private double[] getFeatureVector(double[] wf) {
        double[] fv = Utils.cepstrumCoefficient(wf, NUM_COEFFICIENT);
        for (int i = 0; i < fv.length; i++) fv[i] /= 5000;
        return fv;
    }

    public void save() {
        for (TrainingResult trainingResult: trainingResults) trainingResult.save();
    }
}
