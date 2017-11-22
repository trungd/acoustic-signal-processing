package com.dvtrung.sound.lib;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.StatUtils;

public class MathUtils {
    public static RealMatrix createRandomMatrix(int m, int n) {
        double[][] r = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                r[i][j] = Math.random();
        return MatrixUtils.createRealMatrix(r);
    }

    public static double[][] reshape(double[] arr, int size) {
        double[][] r = new double[arr.length / size][size];
        for (int i = 0; i < arr.length; i++)
            r[i / size][i % size] = arr[i];
        return r;
    }
}
