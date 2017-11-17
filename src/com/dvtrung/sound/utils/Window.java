package com.dvtrung.sound.utils;

public class Window {
    public static double[] gaussian(double sigma, int size) {
        double[] w = new double[size];
        for (int i = 0; i < size; i++)
            w[i] = Math.exp(-1.0 / 2 * Math.pow((i - (size - 1) / 2) / (sigma * (size - 1) / 3), 2));
        return w;
    }
}
