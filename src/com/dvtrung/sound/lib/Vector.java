package com.dvtrung.sound.lib;

public class Vector {
    public static double sum(double[] x) {
        double s = 0;
        for (double t: x) s += t;
        return s;
    }

    public static double sum(double[] x, int start, int end) {
        double s = 0;
        for (int i = start; i <= end; i++) s += x[i];
        return s;
    }

    public static double mean(double[] x) {
        return sum(x) / x.length;
    }

    public static double[] init(int size, double val) {
        double[] r = new double[size];
        for (int i = 0; i < size; i++) r[i] = val;
        return r;
    }

    public static double[] subtract(double[] x, double[] y) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) r[i] = x[i] - y[i];
        return r;
    }

    public static double[] add(double[] x, double[] y) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) r[i] = x[i] + y[i];
        return r;
    }

    public static double[] mul(double[] x, double[] y) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) r[i] = x[i] * y[i];
        return r;
    }

    public static double[] divide(double[] x, double[] y) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) r[i] = x[i] / y[i];
        return r;
    }

    public static double[] square(double[] x) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) r[i] = x[i] * x[i];
        return r;
    }

    /**
     * pearson correlation coefficient
     * @param x
     * @param y
     * @return
     */
    public static double ppc(double[] x, double[] y) {
        int n = x.length;
        double meanX = mean(x);
        double meanY = mean(y);

        double[] _x = subtract(x, init(n, meanX));
        double[] _y = subtract(y, init(n, meanY));
        return (sum(mul(_x, _y)) / Math.sqrt(sum(square(_x))) * Math.sqrt(sum(square(_y))));
    }

    /**
     *
     * @param x
     * @param cnt: even number
     * @return
     */
    public static double[] medianSmoothing(double[] x, int cnt) {
        double[] r = new double[x.length - cnt / 2 * 2];
        for (int i = cnt / 2; i < x.length - cnt / 2; i++) {
            r[i - cnt / 2] = sum(x, i - cnt / 2, i + cnt / 2) / cnt;
        }
        return r;
    }
}
