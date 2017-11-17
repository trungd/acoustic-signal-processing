package com.dvtrung.sound.chart.controllers;

import com.dvtrung.sound.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.LineChartWithSpectrogram;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathArrays;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.dvtrung.sound.utils.Utils.ZERO_CROSSING_VOICED_SOUND_DIFFERENCE_RANGE;
import static com.dvtrung.sound.utils.Utils.zeroCrossingRate;

public class SpectrogramChart extends SoundChart {
    private LineChartWithSpectrogram<Number, Number> chart;
    NumberAxis xAxis, yAxis;
    double[][] specLog;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        xAxis.setLowerBound(0.0);
        yAxis = new NumberAxis();
        yAxis.setLabel("Frequency (Hz)");
        yAxis.setLowerBound(0.0);
        chart = new LineChartWithSpectrogram<Number, Number>(xAxis, yAxis);
        chart.setTitle("Spectrogram");
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        VBox.setVgrow(chart, Priority.ALWAYS);
    }

    @Override
    public void plot(double[] waveform) {
        if (specLog != null && !options.recording) return;

        final double frameDuration = 0.02;
        final double shiftDuration = 0.005;
        final double nyquist = options.sampleRate * 0.5;

        final int frameSize = (int)Math.round(frameDuration * options.sampleRate);
        final int fftSize = 1 << Le4MusicUtils.nextPow2(frameSize);
        final int fftSize2 = (fftSize >> 1) + 1;

        final int shiftSize = (int)Math.round(shiftDuration * options.sampleRate);

        final double[] window = MathArrays.normalizeArray(
                Arrays.copyOf(Le4MusicUtils.hanning(frameSize), fftSize), 1.0
        );

        final Stream<Complex[]> spectrogram = Le4MusicUtils.sliding(waveform, window, shiftSize)
                .map(frame -> Le4MusicUtils.rfft(frame));

        specLog = spectrogram.map(
                sp -> Arrays.stream(sp).mapToDouble(c -> 20.0 * Math.log10(c.abs())).toArray()
        ).toArray(n -> new double[n][]);

        chart.getData().clear();
        chart.getData().add(getVoicedSoundData(waveform));

        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(0.2);
        xAxis.setMinorTickVisible(true);
        xAxis.setUpperBound(specLog.length * shiftDuration);

        yAxis.setAutoRanging(false);
        yAxis.setTickUnit(0);
        yAxis.setUpperBound(nyquist);
        chart.setParameters(specLog.length, fftSize2, nyquist);

        Arrays.stream(specLog).forEach(chart::addSpecLog);
    }

    private XYChart.Series<Number, Number> getVoicedSoundData(double[] waveform) {
        double[] ff = new double[waveform.length / options.frameSize];
        double[] zcr = new double[waveform.length / options.frameSize];

        for (int i = 0; i < options.getFrameCount(); i++) {
            zcr[i] = zeroCrossingRate(waveform, i * options.frameSize) * (options.sampleRate / options.frameSize);
            ff[i] = Utils.getFundamentalFrequency(Utils.getFrameWaveform(waveform, i * options.frameSize));
            int range = ZERO_CROSSING_VOICED_SOUND_DIFFERENCE_RANGE;
            if ((zcr[i] > 2 * ff[i] + range) || (zcr[i] < 2 * ff[i] - range)) ff[i] = 0;
            else ff[i] = options.sampleRate * 0.25;
        }

        ObservableList<XYChart.Data<Number, Number>> data = IntStream.range(0, waveform.length / options.frameSize * 2)
                .mapToObj(i -> new XYChart.Data<Number, Number>((i / 2 + i % 2) * options.frameSize / options.sampleRate, ff[i / 2]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Fundamental Frequency");
        series.setData(data);

        return series;
    }

    @Override
    public Chart getChart() { return chart; }
    public String getTitle() { return "Spectrogram"; }
}
