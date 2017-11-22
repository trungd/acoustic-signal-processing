package com.dvtrung.sound.gui.controllers;

import com.dvtrung.sound.gui.Options;
import com.dvtrung.sound.gui.charts.SignalChart;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class VoiceRecognitionController implements Initializable {
    @FXML private SignalChart wfChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wfChart.setWaveform(Options.getInstance().waveform);
    }
}
