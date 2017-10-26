package com.dvtrung.sound.chart;

import com.dvtrung.sound.Utils;
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

import static com.dvtrung.sound.Utils.zeroCrossingRate;

public class SpectrogramChart extends SoundChart {
    private LineChartWithSpectrogram<Number, Number> chart;

    @Override
    public void init() {
        super.init();

        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        xAxis.setLowerBound(0.0);
        final NumberAxis yAxis = new NumberAxis();
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
        if (!options.bEntirePeriod) return;

        final double frameDuration = Le4MusicUtils.frameDuration;
        final double shiftDuration = frameDuration / 8.0;
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

        final double[][] specLog = spectrogram.map(
                sp -> Arrays.stream(sp).mapToDouble(c -> 20.0 * Math.log10(c.abs())).toArray()
        ).toArray(n -> new double[n][]);

        chart.getData().add(getFundamentalFrequencyData(waveform));

        ((NumberAxis)chart.getXAxis()).setUpperBound(specLog.length * shiftDuration);
        ((NumberAxis)chart.getYAxis()).setUpperBound(nyquist);
        chart.setParameters(specLog.length, fftSize2, nyquist);

        Arrays.stream(specLog).forEach(chart::addSpecLog);
    }

    private XYChart.Series<Number, Number> getFundamentalFrequencyData(double[] waveform) {
        double[] ff = new double[waveform.length / options.frameSize];
        double[] zcr = new double[waveform.length / options.frameSize];

        for (int i = 0; i < options.getFrameCount(); i++) {
            zcr[i] = zeroCrossingRate(waveform, i * options.frameSize) * (options.sampleRate / options.frameSize);
            ff[i] = Utils.getFundamentalFrequency(waveform, i * options.frameSize);
            int range = 400;
            if ((zcr[i] > 2 * ff[i] + range) || (zcr[i] < 2 * ff[i] - range)) ff[i] = 0;
        }

        ObservableList<XYChart.Data<Number, Number>> data = IntStream.range(0, waveform.length / options.frameSize)
                .mapToObj(i -> new XYChart.Data<Number, Number>((i * options.frameSize + options.frameSize / 2) / options.sampleRate, ff[i]))
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
