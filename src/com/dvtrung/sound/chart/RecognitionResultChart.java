package com.dvtrung.sound.chart;

import com.dvtrung.sound.Train;
import com.dvtrung.sound.Utils;
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

public class RecognitionResultChart extends SoundChart {
    private LineChartWithMarkers chart;
    private double maxVal, minVal;
    NumberAxis xAxis, yAxis;

    ObservableList<XYChart.Data<Number, Number>> data;

    XYChart.Data<Number, Number> currentPosMarker;

    Train.TrainingResult[] trainingResults = new Train.TrainingResult[5];

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

        currentPosMarker = new XYChart.Data<Number, Number>(0, 0);
        chart.addVerticalValueMarker(currentPosMarker);

        trainingResults[0] = Train.train("/Users/trung/Desktop/training/a.wav");
        trainingResults[1] = Train.train("/Users/trung/Desktop/training/i.wav");
        trainingResults[2] = Train.train("/Users/trung/Desktop/training/u.wav");
        trainingResults[3] = Train.train("/Users/trung/Desktop/training/e.wav");
        trainingResults[4] = Train.train("/Users/trung/Desktop/training/o.wav");
    }

    @Override
    public void plot(double[] waveform) {
        if (!options.bEntirePeriod) return;

        double[] res = new double[options.getFrameCount()];
        for (int i = 0; i < options.getFrameCount(); i++) {
            double[] loss = new double[5];
            int ret = 0;
            for (int k = 0; k < 5; k++) {
                double[] wf = Arrays.copyOfRange(waveform, i * options.frameSize, i * options.frameSize + options.frameSize);
                double[] x = Utils.cepstrum(wf);
                loss[k] = trainingResults[k].L(x);
                if (loss[k] > loss[ret]) ret = k;
            }
            res[i] = ret;
        }
        data = IntStream.range(0, options.getFrameCount())
                        .mapToObj(i -> new XYChart.Data<Number, Number>(i * options.frameSize / options.sampleRate, res[i]))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Result");
        series.setData(data);

        chart.setCreateSymbols(false);
        chart.getData().clear();
        chart.getData().add(series);

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound((waveform.length - 1) / options.sampleRate);
    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Recognition Result"; }
}
