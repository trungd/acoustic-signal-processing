package com.dvtrung.sound.chart;

import javafx.concurrent.Task;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.LineChartWithSpectrogram;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathArrays;

import java.util.Arrays;
import java.util.stream.Stream;

public class SpectrogramChart extends SoundChart {
    private LineChartWithSpectrogram<Number, Number> spectrogramChart;

    @Override
    public void init() {
        super.init();

        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        xAxis.setLowerBound(0.0);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Frequency (Hz)");
        yAxis.setLowerBound(0.0);
        spectrogramChart = new LineChartWithSpectrogram<Number, Number>(xAxis, yAxis);
        spectrogramChart.setTitle("Spectrogram");
        spectrogramChart.setCreateSymbols(false);
        spectrogramChart.setLegendVisible(false);
        VBox.setVgrow(spectrogramChart, Priority.ALWAYS);
    }

    @Override
    public void plot(double[] waveform, double sampleRate) {
        Task<Void> plot = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                final double frameDuration = Le4MusicUtils.frameDuration;
                final double shiftDuration = frameDuration / 8.0;
                final int frameSize = (int)Math.round(frameDuration * sampleRate);
                final int fftSize = 1 << Le4MusicUtils.nextPow2(frameSize);
                final int fftSize2 = (fftSize >> 1) + 1;

                final int shiftSize = (int)Math.round(shiftDuration * sampleRate);

                final double[] window = MathArrays.normalizeArray(
                        Arrays.copyOf(Le4MusicUtils.hanning(frameSize), fftSize), 1.0
                );

                final Stream<Complex[]> spectrogram = Le4MusicUtils.sliding(waveform, window, shiftSize)
                        .map(frame -> Le4MusicUtils.rfft(frame));

                final double[][] specLog = spectrogram.map(
                        sp -> Arrays.stream(sp).mapToDouble(c -> 20.0 * Math.log10(c.abs())).toArray()
                ).toArray(n -> new double[n][]);

                ((NumberAxis)spectrogramChart.getXAxis()).setUpperBound(specLog.length * shiftDuration);
                ((NumberAxis)spectrogramChart.getYAxis()).setUpperBound(sampleRate * 0.5);
                spectrogramChart.setParameters(specLog.length, fftSize2, sampleRate * 0.5);
                Arrays.stream(specLog).forEach(spectrogramChart::addSpecLog);
                return null;
            }
        };
        new Thread(plot).start();
    }

    @Override
    public Chart getChart() { return spectrogramChart; }
    public String getTitle() { return "Spectrogram"; }
}
