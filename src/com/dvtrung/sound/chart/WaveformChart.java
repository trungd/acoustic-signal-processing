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

public class WaveformChart extends SoundChart {
    private LineChart waveformChart;
    private double maxVal, minVal;
    NumberAxis xAxis, yAxis;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Amplitude");
        waveformChart = new LineChart<Number, Number>(xAxis, yAxis);
        waveformChart.setTitle("Waveform");
        VBox.setVgrow(waveformChart, Priority.ALWAYS);
        waveformChart.setAnimated(false);
    }

    @Override
    public void plot(double[] waveform, double sampleRate) {
        final ObservableList<XYChart.Data<Number, Number>> data =
                IntStream.range(0, waveform.length)
                        .mapToObj(i -> new XYChart.Data<Number, Number>(i / sampleRate, waveform[i]))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Waveform");
        series.setData(data);

        waveformChart.setCreateSymbols(false);
        waveformChart.getData().clear();
        waveformChart.getData().add(series);

        minVal = Math.min(minVal, Arrays.stream(waveform).min().getAsDouble());
        maxVal = Math.max(maxVal, Arrays.stream(waveform).max().getAsDouble());

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(minVal);
        yAxis.setUpperBound(maxVal);

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(waveform.length / sampleRate);
    }

    public Chart getChart() {
        return waveformChart;
    }
    public String getTitle() { return "Waveform"; }
}
