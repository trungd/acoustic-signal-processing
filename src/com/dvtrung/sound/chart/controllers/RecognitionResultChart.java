package com.dvtrung.sound.chart.controllers;

import com.dvtrung.sound.trainingmodels.StatisticalModel;
import com.dvtrung.sound.trainingmodels.TrainingHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RecognitionResultChart extends SoundChart {
    private StackedBarChart<String, Number> chart;
    private double maxVal, minVal;
    CategoryAxis xAxis;
    NumberAxis yAxis;

    ObservableList<XYChart.Data<String, Number>>[] data;

    //XYChart.Data<Number, String> currentPosMarker;

    @Override
    public void init() {
        super.init();

        xAxis = new CategoryAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Label");

        chart = new StackedBarChart<String, Number>(xAxis, yAxis);
        chart.setTitle(getTitle());
        VBox.setVgrow(chart, Priority.ALWAYS);
        chart.setAnimated(false);
        VBox.setVgrow(chart, Priority.ALWAYS);

        //chart.setBarGap(0);
        chart.setCategoryGap(0);

        //currentPosMarker = new XYChart.Data<Number, Number>(0, 0);
        //chart.addVerticalValueMarker(currentPosMarker);
    }

    @Override
    public void plot(double[] waveform) {
        if (data != null) return;

        int frameSize = StatisticalModel.FRAME_LENGTH_IN_MILIS * (int)options.sampleRate / 1000;
        int frameCount = waveform.length / frameSize;
        String[] res = new String[frameCount];
        for (int i = 0; i < frameCount; i++) {
            res[i] = TrainingHelper.getInstance().getLabel(waveform, i * frameSize);
        }

        String[] labels = {"a", "i", "u", "e", "o"};
        data = new ObservableList[labels.length];

        chart.getData().clear();
        for (int k = 0; k < labels.length; k++) {
            int finalK = k;
            data[k] = IntStream.range(0, frameCount)
                    .mapToObj(i -> new XYChart.Data<String, Number>(String.valueOf(i * frameSize / options.sampleRate), res[i].equals(labels[finalK]) ? 1 : 0))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            final XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(labels[k]);
            series.setData(data[k]);

            chart.getData().add(series);
        }

        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(1);
    }

    @Override
    public void onRecordingChanged(boolean recording) {
        if (recording) getCheckBox().setSelected(false);
        getCheckBox().setDisable(recording);
    }

    public Chart getChart() {
        return chart;
    }
    public String getTitle() { return "Recognition Result"; }
}
