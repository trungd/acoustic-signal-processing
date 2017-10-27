package com.dvtrung.sound.controller;

import com.dvtrung.sound.Options;
import com.dvtrung.sound.Utils;
import com.dvtrung.sound.chart.*;
import com.dvtrung.sound.train.TrainingHelper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Player;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML public BorderPane rootPane;
    @FXML private VBox chartPane;
    @FXML private Label statusLabel;
    @FXML private VBox checkBoxPane;
    @FXML private Slider posSlider;
    @FXML private Label fundamentalFrequencyLabel, loudnessLabel, vowelLabel;
    @FXML private Label textFrameDuration;

    private WaveformChart waveformChart;
    private WaveformFrameChart waveformFrameChart;
    private SpectrumChart spectrumChart;
    private SpectrogramChart spectrogramChart;
    private AutocorrelationChart autocorrelationChart;
    private CepstrumChart cepstrumChart;
    private RealCepstrumChart realCepstrumChart;
    private RecognitionResultChart recognitionResultChart;

    private Options options;

    private ArrayList<SoundChart> arrChart = new ArrayList<>();

    File wavFile;
    AudioFormat format;

    private Player player;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        options = Options.getInstance();
        Options.getInstance().currentPos = 0;
        Options.getInstance().frameSize = 5000;
        posSlider.setValue(Options.getInstance().currentPos);

        posSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setPos(newValue.intValue());
            }
        });

        posSlider.setOnMouseDragExited(new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent event) {
                waveformChart.refreshMarker();
            }
        });

        textFrameDuration.setText(String.valueOf(options.getFrameDurationInMilis()));
    }

    private void setPos(int pos) {
        Options.getInstance().currentPos = pos;
        plot();
    }

    @FXML
    private final void handleChartToggleAction(final ActionEvent ev) {
        chartPane.getChildren().clear();
        for (SoundChart chart: arrChart) {
            if (chart.getCheckBox().isSelected()) {
                chartPane.getChildren().add(chart.getChart());
                plot();
            }
        }
    }

    @FXML
    private final void handlePlay(final ActionEvent ev) {
        player.start();
    }

    @FXML
    private final void handlePause(final ActionEvent ev) {
        player.stop();
    }

    @FXML
    private final void handleExitAction(final ActionEvent ev) {
        Platform.exit();
    }

    @FXML
    private final void handleOpenWaveFileAction(final ActionEvent ev) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open WAV File");
        //fileChooser.setInitialDirectory(new File("/Users/trung/Desktop"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Wav Files", "*.wav"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        wavFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (wavFile == null) return;
        else openFile(wavFile);

        try {
            player = Player.builder(wavFile).build();

            player.addAudioFrameListener((frame, position) -> {
                posSlider.setValue(position);
                setPos(position);
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        plot();
    }

    /**
     * Plot all visible charts
     */
    private void plot() {
        if (options.waveform == null) return;

        statusLabel.setText("Plotting...");
        for (SoundChart chart: arrChart) {
            if (chart.getCheckBox().isSelected())
                chart.plot(options.waveform);
        }

        fundamentalFrequencyLabel.setText(
                String.valueOf(Utils.getFundamentalFrequency(options.waveform, options.currentPos)) + "Hz");
        loudnessLabel.setText(Utils.getLoudness() + "dB");
        vowelLabel.setText(TrainingHelper.getInstance().getLabel(options.waveform, options.currentPos));

        statusLabel.setText("Ready");
    }

    private void openFile(String fileName) {
        File file = new File(fileName);
        openFile(file);
    }

    private void openFile(File file) {
        try (final AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
            format = stream.getFormat();
            options.waveform = Le4MusicUtils.readWaveformMonaural(stream);
            Options.getInstance().sampleRate = stream.getFormat().getSampleRate();
            Options.getInstance().frameSize = (int)(Options.getInstance().sampleRate / 50);
            posSlider.setMin(0);
            posSlider.setMax(options.waveform.length - options.frameSize - 1);

            resetChart();
            textFrameDuration.setText(String.valueOf(options.getFrameDurationInMilis()));


        } catch (IOException e) {
            Le4MusicUtils.showAlertAndWait("I/O Error: " + file, e);
            return;
        } catch (UnsupportedAudioFileException e) {
            Le4MusicUtils.showAlertAndWait("Unsupported Audio File: " + file, e);
            return;
        }
    }

    /**
     * Create new charts on file open
     */
    private void resetChart() {
        waveformChart = new WaveformChart();
        waveformFrameChart = new WaveformFrameChart();
        spectrumChart = new SpectrumChart();
        spectrogramChart = new SpectrogramChart();
        autocorrelationChart = new AutocorrelationChart();
        cepstrumChart = new CepstrumChart();
        realCepstrumChart = new RealCepstrumChart();
        recognitionResultChart = new RecognitionResultChart();

        arrChart.clear();
        arrChart.add(waveformChart);
        arrChart.add(waveformFrameChart);
        arrChart.add(spectrumChart);
        arrChart.add(spectrogramChart);
        arrChart.add(autocorrelationChart);
        arrChart.add(cepstrumChart);
        arrChart.add(realCepstrumChart);
        arrChart.add(recognitionResultChart);

        checkBoxPane.getChildren().clear();
        for (SoundChart chart: arrChart) {
            chart.init();
            chart.getCheckBox().setOnAction(e -> handleChartToggleAction(e));
            checkBoxPane.getChildren().add(chart.getCheckBox());
        }

        waveformChart.getCheckBox().setSelected(true);
        handleChartToggleAction(null);
    }

    @FXML
    private void handleChangeFrameDuration(final ActionEvent ev) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(options.getFrameDurationInMilis()));
        dialog.setTitle("Frame Duration");
        dialog.setHeaderText("Frame Duration in Miliseconds");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            options.setFrameDurationInMilis(Integer.parseInt(result.get()));
            textFrameDuration.setText(result.get());
            posSlider.setMax(options.waveform.length - options.frameSize - 1);
        }
    }
}
