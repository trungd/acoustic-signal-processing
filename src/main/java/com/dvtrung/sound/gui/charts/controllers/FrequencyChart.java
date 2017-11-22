package com.dvtrung.sound.gui.charts.controllers;

import com.dvtrung.sound.lib.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FrequencyChart extends SoundChart {
    private BarChart<String, Number> chart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private static int FRAME_COUNT = 30;

    double[] loudnesses = new double[FRAME_COUNT];
    int initCount = 0;

    @Override
    public void init() {
        super.init();

        xAxis = new CategoryAxis();
        xAxis.setLabel("Time (s)");

        yAxis = new NumberAxis();
        yAxis.setLabel("Loudness");

        chart = new BarChart<String, Number>(xAxis, yAxis);
        chart.setTitle(getTitle());
        VBox.setVgrow(chart, Priority.ALWAYS);
        chart.setAnimated(false);
        chart.setBarGap(0);
        chart.setCategoryGap(0);
    }

    @Override
    public void plot(double[] wf) {
        if (initCount < FRAME_COUNT) {
            loudnesses[initCount] = Utils.getFundamentalFrequency(wf);
            initCount++;
        } else {
            for (int i = 1; i < FRAME_COUNT; i++) loudnesses[i - 1] = loudnesses[i];
            loudnesses[FRAME_COUNT - 1] = Utils.getFundamentalFrequency(wf);
        }

        ObservableList<XYChart.Data<String, Number>>  data_loudness = IntStream.range(0, loudnesses.length)
                .mapToObj(i -> new XYChart.Data<String, Number>(String.valueOf(i), loudnesses[i]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        final XYChart.Series<String, Number> series_loudness = new XYChart.Series<>("Frequency", data_loudness);

        chart.getData().clear();
        chart.getData().add(series_loudness);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setTickUnit(100);
        yAxis.setMinorTickCount(1);
        yAxis.setUpperBound(1000);
    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Fundamental Frequency"; }
}
