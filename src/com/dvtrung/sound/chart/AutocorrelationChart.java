package com.dvtrung.sound.chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AutocorrelationChart extends SoundChart {
    private LineChart autocorrelationChart;
    private double[] ac;
    private double sampleRate;
    NumberAxis xAxis, yAxis;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Amplitude");
        autocorrelationChart = new LineChart<Number, Number>(xAxis, yAxis);
        autocorrelationChart.setTitle("Autocorrelation");
        autocorrelationChart.setAnimated(false);
        VBox.setVgrow(autocorrelationChart, Priority.ALWAYS);
    }

    @Override
    public void plot(double[] waveform, double sampleRate) {
        ac = new double[waveform.length];
        Arrays.fill(ac, 0);
        int n = waveform.length;
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n - i; j++) {
                ac[i] += waveform[j] * waveform[j - i];
            }
        }

        final ObservableList<XYChart.Data<Number, Number>> data =
                IntStream.range(0, waveform.length)
                        .mapToObj(i -> new XYChart.Data<Number, Number>(i / sampleRate, ac[i]))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Waveform");
        series.setData(data);

        autocorrelationChart.setCreateSymbols(false);
        autocorrelationChart.getData().clear();
        autocorrelationChart.getData().add(series);

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(waveform.length / sampleRate);

        this.sampleRate = sampleRate;
    }

    public Chart getChart() {
        return autocorrelationChart;
    }
    public String getTitle() { return "Autocorrelation"; }

    public int getFundamentalFrequency() {
        int start = 0;
        while (ac[start] >= ac[start + 1]) start += 1;

        int secondPeak = start;
        for (int i = start; i < ac.length - 1; i++) {
            if (ac[secondPeak] < ac[i]) secondPeak = i;
        }

        /*
        for (int i = 1; i < ac.length - 1; i++) {
            if ((ac[i - 1] < ac[i] && ac[i] > ac[i + 1]) &&
                    ((ac[i - 10] < ac[i] && ac[i] > ac[i + 10]))) {
                secondPeak = i;
                break;
            }
        }
        */

        return (int)(1 / (secondPeak / sampleRate));
    }
}
