package com.dvtrung.sound.utils;

public class Matrix {
    public int m, n;
    public double[][] e;

    public Matrix(int m, int n) {
        this.m = m; this.n = n;
        e = new double[m][n];
    }

    public Matrix(double[][] A) {
        e = A;
        m = A.length; n = A[0].length;
    }

    public Matrix clone() {
        Matrix r = new Matrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                r.e[i][j] = e[i][j];
        return r;
    }

    public void print() {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) System.out.print(String.valueOf(e[i][j]) + " ");
            System.out.println();
        }
    }

    public static Matrix random(int m, int n) {
        Matrix r = new Matrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                r.e[i][j] = Math.random();
        return r;
    }

    public static Matrix reshapeArr(double[] arr, int size) {
        Matrix r = new Matrix(arr.length / size, size);
        for (int i = 0; i < arr.length; i++)
            r.e[i / size][i % size] = arr[i];
        return r;
    }

    public double[] toArray() {
        double[] r = new double[m * n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) r[i * n + j] = e[i][j];
        return r;
    }

    public Matrix T() {
        Matrix r = new Matrix(n, m);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) r.e[j][i] = e[i][j];
        return r;
    }

    public Matrix col(int j) {
        Matrix ret = new Matrix(m, 1);
        for (int k = 0; k < m; k++) ret.e[k][0] = e[k][j];
        return ret;
    }

    public Matrix row(int i) {
        Matrix ret = new Matrix(1, n);
        for (int k = 0; k < n; k++) ret.e[0][k] = e[i][k];
        return ret;
    }

    public static Matrix mul(Matrix a1, Matrix a2) {
        if (a1.n != a2.m) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix ret = new Matrix(a1.m, a2.n);
        for (int i = 0; i < a1.m; i++)
            for (int j = 0; j < a2.n; j++)
                for (int k = 0; k < a1.n; k++)
                    ret.e[i][j] += a1.e[i][k] * a2.e[k][j];
        return ret;
    }

    public Matrix subtract(Matrix a) {
        Matrix ret = new Matrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                ret.e[i][j] = e[i][j] - a.e[i][j];
        return ret;
    }

    public double frobeniusNorm() {
        double[] maxCol = new double[n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                if (e[i][j] > maxCol[j]) maxCol[j] = e[i][j];

        double max = 0;
        for (int i = 0; i < n; i++) if (maxCol[i] > max) max = maxCol[i];
        return max;
    }

    public Matrix mul(Matrix a) {
        return mul(this, a);
    }

    public static Matrix dot(Matrix a1, Matrix a2) {
        if ((a1.n != a2.n) || (a1.m != a2.m)) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix ret = new Matrix(a1.m, a2.n);
        for (int i = 0; i < a1.m; i++)
            for (int j = 0; j < a1.n; j++)
                ret.e[i][j] = a1.e[i][j] * a2.e[i][j];
        return ret;
    }

    public static class NMFResult {
        public Matrix W, H;
        public NMFResult(Matrix W, Matrix H) {
            this.W = W; this.H = H;
        }
    }

    public NMFResult NMF(int k) {
        Matrix W = Matrix.random(m, k);
        Matrix H = Matrix.random(k, n);

        Matrix prevW = new Matrix(m, k);
        Matrix prevH = new Matrix(k, n);

        double e = 0.00000001;
        while ((W.subtract(prevW).frobeniusNorm() > e) || (H.subtract(prevH).frobeniusNorm() > e)) {
            prevH = H.clone();
            prevW = W.clone();

            Matrix A1 = mul(W.T(), this);
            Matrix B1 = W.T().mul(W).mul(H);
            for (int i = 0; i < H.m; i++) for (int j = 0; j < H.n; j++)
                H.e[i][j] = H.e[i][j] * (A1.e[i][j] / B1.e[i][j]);

            Matrix A2 = this.mul(H.T());
            Matrix B2 = W.mul(H).mul(H.T());
            for (int i = 0; i < W.m; i++) for (int j = 0; j < W.n; j++)
                W.e[i][j] = W.e[i][j] * (A2.e[i][j] / B2.e[i][j]);
        }

        return new NMFResult(W, H);
    }
}
