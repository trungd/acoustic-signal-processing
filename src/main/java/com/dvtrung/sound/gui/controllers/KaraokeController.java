package com.dvtrung.sound.gui.controllers;

import com.dvtrung.sound.gui.charts.FrequencyChart;
import com.dvtrung.sound.gui.charts.SpectrogramChart;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import jp.ac.kyoto_u.kuis.le4music.Recorder;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KaraokeController implements Initializable {
    private static double frameInterval = 0.04;
    public static double sampleRate = 16000.00;

    public ProgressBar progressBar;
    public FrequencyChart frequencyChart;
    public Button btnStart;
    public Button btnStop;
    public SpectrogramChart spectrogramChart;
    private boolean bRecording;

    Recorder recorder;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Recorder.Builder builder = Recorder.builder();
        builder.sampleRate((float) sampleRate);
        builder.frameDuration(frameInterval);
        builder.frameSize((int) (frameInterval * sampleRate));
        try {
            recorder = builder.build();
            recorder.addAudioFrameListener((frame, position) -> executor.execute(() -> {
                Platform.runLater(() -> update(frame));
            }));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        btnStop.setDisable(true);
    }

    private void update(double[] frame) {
        frequencyChart.addWaveform(frame);
        //spectrogramChart.addWaveform(frame);
    }

    @FXML
    public void handleStart(ActionEvent actionEvent) {
        Platform.runLater(recorder::start);
        bRecording = true;

        // Update UI
        btnStart.setDisable(true);
        btnStop.setDisable(false);
    }

    public void handleStop(ActionEvent actionEvent) {
        Platform.runLater(recorder::stop);
        bRecording = false;

        // Update UI
        btnStart.setDisable(false);
        btnStop.setDisable(true);
    }
}
