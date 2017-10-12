package com.dvtrung.sound.controller;

import com.dvtrung.sound.chart.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML public BorderPane rootPane;
    @FXML private VBox chartPane;
    @FXML private Label statusLabel;
    @FXML private VBox checkBoxPane;
    @FXML private Slider posSlider;
    @FXML private Label fundamentalFrequencyLabel, loudnessLabel;

    private WaveformChart waveformChart = new WaveformChart();
    private SpectrumChart spectrumChart = new SpectrumChart();
    private SpectrogramChart spectrogramChart = new SpectrogramChart();
    private LoudnessChart loudnessChart = new LoudnessChart();
    private AutocorrelationChart autocorrelationChart = new AutocorrelationChart();

    private ArrayList<SoundChart> arrChart = new ArrayList<>();

    @FXML private CheckBox waveformCheckBox, spectrumCheckBox, spectrogramCheckBox, loudnessCheckBox;

    private double[] waveform;
    AudioFormat format;
    private double sampleRate = Le4MusicUtils.sampleRate;

    private double currentPos;
    private int windowSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        arrChart.add(waveformChart);
        arrChart.add(spectrumChart);
        arrChart.add(spectrogramChart);
        arrChart.add(loudnessChart);
        arrChart.add(autocorrelationChart);

        currentPos = 0;
        windowSize = 5000;
        posSlider.setValue(currentPos);

        posSlider.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                currentPos = posSlider.getValue();
                plot((int)currentPos, windowSize);
            }
        });

        for (SoundChart chart: arrChart) {
            chart.init();
            chart.getCheckBox().setOnAction(e -> handleChartToggleAction(e));
            checkBoxPane.getChildren().add(chart.getCheckBox());
        }

        waveformChart.getCheckBox().setSelected(true);
        autocorrelationChart.getCheckBox().setSelected(true);
        handleChartToggleAction(null);
    }

    @FXML
    private final void handleChartToggleAction(final ActionEvent ev) {
        chartPane.getChildren().clear();
        for (SoundChart chart: arrChart) {
            if (chart.getCheckBox().isSelected())
                chartPane.getChildren().add(chart.getChart());
        }
    }

    @FXML
    private final void handleSaveChartAsImageFileAction(final ActionEvent ev) {

    }

    @FXML
    private final void handleExitAction(final ActionEvent ev) {
        Platform.exit();
    }

    @FXML
    private final void handleResetZoomAction(final  ActionEvent ev) {
        //xAxis.setLowerBound(0.0);
        //xAxis.setUpperBound(sampleRate * 0.5);
        //yAxis.setLowerBound(Le4MusicUtils.spectrumAmplitudeLowerBound);
        //yAxis.setUpperBound(Le4MusicUtils.spectrumAmplitudeUpperBound);
    }

    @FXML
    private final void handleOpenWaveFileAction(final ActionEvent ev) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open WAV File");
        fileChooser.setInitialDirectory(new File("/Users/trung/Desktop"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Wav Files", "*.wav"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        final File wavFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (wavFile == null) return;
        else openFile(wavFile);
        plot(0, windowSize);
    }

    private void plot(int start, int length) {
        double[] wf = new double[length];
        System.arraycopy(waveform, start, wf, 0, length);

        statusLabel.setText("Plotting...");
        for (SoundChart chart: arrChart) {
            if (chart.getCheckBox().isSelected())
                chart.plot(wf, sampleRate);
        }

        fundamentalFrequencyLabel.setText(
                String.valueOf(autocorrelationChart.getFundamentalFrequency()) + "Hz");
        loudnessLabel.setText(loudnessChart.getLoudness() + "dB");

        statusLabel.setText("Ready");
    }

    private void openFile(String fileName) {
        File file = new File(fileName);
        openFile(file);
    }

    private void openFile(File file) {
        try (final AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
            format = stream.getFormat();
            waveform = Le4MusicUtils.readWaveformMonaural(stream);
            sampleRate = stream.getFormat().getSampleRate();
            windowSize = (int)(sampleRate / 10);
            posSlider.setMin(0);
            posSlider.setMax(waveform.length);
        } catch (IOException e) {
            Le4MusicUtils.showAlertAndWait("I/O Error: " + file, e);
            return;
        } catch (UnsupportedAudioFileException e) {
            Le4MusicUtils.showAlertAndWait("Unsupported Audio File: " + file, e);
            return;
        }
    }
}
