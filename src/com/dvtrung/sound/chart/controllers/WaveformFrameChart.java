package com.dvtrung.sound.chart.controllers;

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

    public void plotRecording(double[] frame, int position) {
        if (data == null) {
            data = IntStream.range(-frame.length, 0).mapToDouble(i -> i / options.sampleRate)
                    .mapToObj(t -> new XYChart.Data<Number, Number>(t, 0.0))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            final XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Waveform");
            series.setData(data);

            chart.setCreateSymbols(false);
            chart.getData().clear();
            chart.getData().add(series);

            minVal = 0;
            maxVal = 0;
        }

        IntStream.range(0, frame.length).forEach(i -> {
            final XYChart.Data<Number, Number> datum = data.get(i);
            datum.setXValue((i + position - frame.length) / options.sampleRate);
            datum.setYValue(frame[i]);

            final double posInSec = position / options.sampleRate;

            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(posInSec - options.getFrameDuration());
            xAxis.setUpperBound(posInSec);

            minVal = Math.min(minVal, Arrays.stream(frame).min().getAsDouble());
            maxVal = Math.max(maxVal, Arrays.stream(frame).max().getAsDouble());

            yAxis.setAutoRanging(false);
            double bound = Math.max(maxVal, -minVal);
            yAxis.setLowerBound(-bound);
            yAxis.setUpperBound(+bound);
        });
    }

    @Override
    public void onRecordingChanged(boolean recording) {
        getCheckBox().setSelected(true);
    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Waveform (frame)"; }
}
