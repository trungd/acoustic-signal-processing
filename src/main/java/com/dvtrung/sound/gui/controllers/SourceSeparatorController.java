package com.dvtrung.sound.gui.controllers;

import com.dvtrung.sound.gui.Options;
import com.dvtrung.sound.gui.charts.SignalChart;
import com.dvtrung.sound.lib.SourceSeparatorHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SourceSeparatorController implements Initializable {
    @FXML private VBox sourceChartPane;
    @FXML private SignalChart wfChart;
    private SignalChart[] charts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wfChart.setWaveform(Options.getInstance().waveform);
    }

    @FXML
    private final void handleAnalyse(final ActionEvent ev) {
        double[] wf = Options.getInstance().waveform;

        int numSource = 2;

        SourceSeparatorHelper sourceSeparatorHelper = new SourceSeparatorHelper(3);
        sourceSeparatorHelper.separate(wf, Options.getInstance().frameSize);

        charts = new SignalChart[numSource];
        for (int i = 0; i < numSource; i++) {
            charts[i] = new SignalChart();
            charts[i].setWaveform(sourceSeparatorHelper.getSource(i));

            //charts[i].yAxis.setAutoRanging(false);
            //charts[i].yAxis.setUpperBound(wfChart.yAxis.getUpperBound());
            //charts[i].yAxis.setLowerBound(wfChart.yAxis.getLowerBound());

            sourceChartPane.getChildren().add(charts[i]);
        }
    }
}