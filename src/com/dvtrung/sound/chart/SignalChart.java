package com.dvtrung.sound.chart;

import com.dvtrung.sound.Options;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SignalChart extends LineChart<Number, Number> {
    private double maxVal, minVal;
    public NumberAxis xAxis, yAxis;

    ObservableList<XYChart.Data<Number, Number>> data;

    public SignalChart() {
        super(new NumberAxis(), new NumberAxis());
        xAxis = (NumberAxis)getXAxis();
        //xAxis.setLabel("Time (seconds)");
        yAxis = (NumberAxis)getYAxis();
        //yAxis.setLabel("Amplitude");
        //setTitle("Waveform");
        setAnimated(false);
    }

    public void setWaveform(double[] wf) {
        if (wf == null) return;
        if (data != null) return;
        double sampleRate = Options.getInstance().sampleRate;

        data = IntStream.range(0, wf.length)
                        .mapToObj(i -> new XYChart.Data<Number, Number>(i / sampleRate, wf[i]))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Waveform");
        series.setData(data);

        setCreateSymbols(false);
        getData().clear();
        getData().add(series);

        minVal = Math.min(minVal, Arrays.stream(wf).min().getAsDouble());
        maxVal = Math.max(maxVal, Arrays.stream(wf).max().getAsDouble());

        yAxis.setAutoRanging(false);
        double bound = Math.max(maxVal, -minVal);
        yAxis.setLowerBound(-bound);
        yAxis.setUpperBound(+bound);

        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(0.2);
        xAxis.setMinorTickVisible(true);
        xAxis.setUpperBound((wf.length - 1) / sampleRate);
    }
}
