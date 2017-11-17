package com.dvtrung.sound.chart.controllers;

import com.dvtrung.sound.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jp.ac.kyoto_u.kuis.le4music.LineChartWithMarkers;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dvtrung.sound.utils.Utils.getFrameWaveform;

public class AutocorrelationChart extends SoundChart {
    private LineChartWithMarkers chart;
    private double[] ac;
    NumberAxis xAxis, yAxis;
    XYChart.Data<Number, Number> secondPeakMarker;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (s)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Amplitude");
        chart = new LineChartWithMarkers(xAxis, yAxis);
        chart.setTitle("Autocorrelation");
        chart.setAnimated(false);
        VBox.setVgrow(chart, Priority.ALWAYS);

        secondPeakMarker = new XYChart.Data<>(0,0);
        chart.addVerticalValueMarker(secondPeakMarker);
    }

    @Override
    public void plot(double[] waveform) {

        double wf[] = getFrameWaveform(waveform);
        ac = new double[wf.length];
        Arrays.fill(ac, 0);
        int n = wf.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - i; j++) {
                ac[i] += wf[j] * wf[j + i];
            }
        }

        secondPeakMarker.setXValue(1.0 / Utils.getFundamentalFrequency(waveform));

        final ObservableList<XYChart.Data<Number, Number>> data =
                IntStream.range(0, wf.length)
                        .mapToObj(i -> new XYChart.Data<Number, Number>(i / options.sampleRate, ac[i]))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Waveform");
        series.setData(data);

        chart.setCreateSymbols(false);
        chart.getData().clear();
        chart.getData().add(series);

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(wf.length / options.sampleRate);
    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Autocorrelation (frame)"; }
}
