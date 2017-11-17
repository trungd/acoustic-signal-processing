package com.dvtrung.sound.chart;

import com.dvtrung.sound.Options;
import com.dvtrung.sound.chart.controllers.SoundChart;
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

public class SpectrogramChart extends LineChartWithSpectrogram<Number, Number> {
    NumberAxis xAxis, yAxis;
    double[][] specLog;

    private static int FRAME_COUNT = 20;
    int initCount = 0;
    double[][][] specLogs = new double[FRAME_COUNT][][];

    public SpectrogramChart() {
        super(new NumberAxis(), new NumberAxis());

        xAxis = (NumberAxis)getXAxis();
        xAxis.setLabel("Time (seconds)");
        xAxis.setLowerBound(0.0);

        yAxis = (NumberAxis)getYAxis();
        yAxis.setLabel("Frequency (Hz)");
        yAxis.setLowerBound(0.0);

        setTitle("Spectrogram");
        setCreateSymbols(false);
        setLegendVisible(false);

        VBox.setVgrow(this, Priority.ALWAYS);
    }

    public void addWaveform(double[] waveform) {
        Options options = Options.getInstance();

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

        if (initCount < FRAME_COUNT) {
            specLogs[initCount] = specLog;
            initCount++;
        } else {
            for (int i = 1; i < FRAME_COUNT; i++) specLogs[i - 1] = specLogs[i];
            specLogs[FRAME_COUNT - 1] = specLog;
        }

        getData().clear();
        //getData().add(getVoicedSoundData(waveform));

        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(0.2);
        xAxis.setMinorTickVisible(true);
        xAxis.setUpperBound(specLog.length * FRAME_COUNT * shiftDuration);

        yAxis.setAutoRanging(false);
        yAxis.setTickUnit(0);
        yAxis.setUpperBound(nyquist);

        setParameters(specLog.length * FRAME_COUNT, fftSize2, nyquist);

        for (double[][] s: specLogs) if (s != null) Arrays.stream(s).forEach(this::addSpecLog);
    }
}
