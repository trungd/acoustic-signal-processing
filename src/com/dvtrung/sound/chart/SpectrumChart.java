package com.dvtrung.sound.chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import org.apache.commons.math3.complex.Complex;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpectrumChart extends SoundChart {
    private LineChart spectrumChart;
    NumberAxis xAxis, yAxis;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Frequency (Hz)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Amplitude (dB)");

        /*
        final DoubleProperty xLo = xAxis.lowerBoundProperty();
        final DoubleProperty xUp = xAxis.upperBoundProperty();
        xAxis.tickUnitProperty().bind(new DoubleBinding() {
            {
                super.bind(xLo, xUp);
            }
            @Override
            protected double computeValue() {
                return Le4MusicUtils.autoTickUnit(xUp.get() - xLo.get());
            }
        });

        final DoubleProperty yLo = yAxis.lowerBoundProperty();
        final DoubleProperty yUp = yAxis.upperBoundProperty();
        yAxis.tickUnitProperty().bind(new DoubleBinding() {
            {
                super.bind(yLo, yUp);
            }
            @Override
            protected double computeValue() {
                return Le4MusicUtils.autoTickUnit(yUp.get() - yLo.get());
            }
        });

        final double magnifyScale = 2.0;
        xAxis.setOnScroll(ev -> {
            final double center = xAxis.getValueForDisplay(ev.getX()).doubleValue();
            final double factor = (ev.getDeltaY() < 0.0) ? magnifyScale : 1.0 / magnifyScale;
            xAxis.setLowerBound(center - (center - xAxis.getLowerBound()) * factor);
            xAxis.setUpperBound(center + (xAxis.getUpperBound() - center) * factor);
        });
        yAxis.setOnScroll(ev -> {
            final double center = yAxis.getValueForDisplay(ev.getY()).doubleValue();
            final double factor = (ev.getDeltaY() < 0.0) ? magnifyScale : 1.0 / magnifyScale;
            yAxis.setLowerBound(center - (center - yAxis.getLowerBound()) * factor);
            yAxis.setUpperBound(center + (yAxis.getUpperBound() - center) * factor);
        });
        */

        spectrumChart = new LineChart<Number, Number>(xAxis, yAxis);
        spectrumChart.setTitle("Spectrum");
        spectrumChart.setCreateSymbols(false);
        spectrumChart.setAnimated(false);
        VBox.setVgrow(spectrumChart, Priority.ALWAYS);
    }

    @Override
    public void plot(double[] waveform) {
        final double maxFreq = 3000;
        final int fftSize = 1 << Le4MusicUtils.nextPow2(waveform.length);
        final int fftSize2 = (fftSize >> 1) + 1;

        final double[] src = Arrays.stream(Arrays.copyOf(waveform, fftSize))
                .map(w -> w / waveform.length)
                .toArray();

        Complex[] spectrum = Le4MusicUtils.rfft(src);
        int len = (int)(maxFreq * fftSize / options.sampleRate);

        final double[] specLog = Arrays.stream(spectrum)
                .mapToDouble(c -> Math.log10(c.abs()))
                .toArray();

        final ObservableList<XYChart.Data<Number, Number>> data = IntStream.range(0, len)
                .mapToObj(i -> new XYChart.Data<Number, Number>(i * options.sampleRate / fftSize, specLog[i]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>("Spectrum", data);

        spectrumChart.getData().clear();
        spectrumChart.getData().clear();
        spectrumChart.getData().add(series);

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(len * options.sampleRate / fftSize);
        xAxis.setTickUnit(100);
    }

    @Override
    public Chart getChart() {
        return spectrumChart;
    }
    public String getTitle() { return "Spectrum"; }
}
