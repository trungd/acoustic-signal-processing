package com.dvtrung.sound.chart;

import com.dvtrung.sound.Options;
import com.dvtrung.sound.Utils;
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

public class FundamentalFrequencyChart extends SoundChart {
    private LineChart chart;
    private double[] ac;
    NumberAxis xAxis, yAxis;

    ObservableList<XYChart.Data<Number, Number>> data;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Frequency (Hz)");
        chart = new LineChart<Number, Number>(xAxis, yAxis);
        chart.setTitle("Fundamental Frequency");
        chart.setAnimated(false);
        VBox.setVgrow(chart, Priority.ALWAYS);
    }

    @Override
    public void plot(double[] waveform) {
        if (waveform.length == data.size()) return;

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(waveform.length / options.sampleRate);

        chart.setCreateSymbols(false);

        chart.getData().clear();

    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Fundamental Frequency"; }
}
