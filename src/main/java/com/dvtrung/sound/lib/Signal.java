package com.dvtrung.sound.lib;

public class Signal {
    public static double[] downsample(double[] x, int times) {
        double[] ret = new double[x.length / times];
        for (int i = 0; i < ret.length; i++) ret[i] = x[i * times];
        return ret;
    }
}
