package com.dvtrung.sound.chart;

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

public class WaveformFrameChart extends SoundChart {
    private LineChartWithMarkers chart;
    private double maxVal, minVal;
    NumberAxis xAxis, yAxis;

    ObservableList<XYChart.Data<Number, Number>> data;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Amplitude");
        chart = new LineChartWithMarkers<Number, Number>(xAxis, yAxis);
        chart.setTitle("Waveform");
        VBox.setVgrow(chart, Priority.ALWAYS);
        chart.setAnimated(false);
        VBox.setVgrow(chart, Priority.ALWAYS);
    }

    @Override
    public void plot(double[] waveform) {
        double wf[] = getFrameWaveform(waveform);

        data = IntStream.range(0, wf.length)
                        .mapToObj(i -> new XYChart.Data<Number, Number>(i / options.sampleRate, wf[i]))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Waveform");
        series.setData(data);

        chart.setCreateSymbols(false);
        chart.getData().clear();
        chart.getData().add(series);

        minVal = Math.min(minVal, Arrays.stream(wf).min().getAsDouble());
        maxVal = Math.max(maxVal, Arrays.stream(wf).max().getAsDouble());

        yAxis.setAutoRanging(false);
        double bound = Math.max(maxVal, -minVal);
        yAxis.setLowerBound(-bound);
        yAxis.setUpperBound(+bound);

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound((wf.length - 1) / options.sampleRate);
    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Waveform (frame)"; }
}
