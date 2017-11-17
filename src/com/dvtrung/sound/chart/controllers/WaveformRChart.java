package com.dvtrung.sound.chart.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WaveformRChart extends SoundChart {
    private LineChart<Number, Number> chart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private static int FRAME_COUNT = 20;

    double[][] wfs = new double[FRAME_COUNT][];
    int initCount = 0;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");

        yAxis = new NumberAxis();
        yAxis.setLabel("Loudness");

        chart = new LineChart<Number, Number>(xAxis, yAxis);
        chart.setTitle(getTitle());
        VBox.setVgrow(chart, Priority.ALWAYS);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
    }

    @Override
    public void plot(double[] wf) {
        if (initCount < FRAME_COUNT) {
            wfs[initCount] = wf;
            initCount++;
        } else {
            for (int i = 1; i < FRAME_COUNT; i++) wfs[i - 1] = wfs[i];
            wfs[FRAME_COUNT - 1] = wf;
        }

        double[] _wf = new double[wf.length * FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) for (int j = 0; j < wfs[i].length; j++)
            _wf[i * wf.length + j] = wfs[i][j];
        ObservableList<XYChart.Data<Number, Number>> data = IntStream.range(0, _wf.length)
                .mapToObj(i -> new XYChart.Data<Number, Number>(i, _wf[i]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        final XYChart.Series<Number, Number> series = new XYChart.Series<>("Waveform", data);

        chart.getData().clear();
        chart.getData().add(series);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-1);
        yAxis.setUpperBound(1);
        yAxis.setTickUnit(1);
    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Waveform"; }
}
