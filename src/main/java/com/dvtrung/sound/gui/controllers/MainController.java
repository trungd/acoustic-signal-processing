package com.dvtrung.sound.gui.controllers;

import com.dvtrung.sound.gui.Options;
import com.dvtrung.sound.gui.charts.controllers.*;
import com.dvtrung.sound.lib.Utils;
import com.dvtrung.sound.lib.trainingmodels.TrainingHelper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Player;
import jp.ac.kyoto_u.kuis.le4music.Recorder;
import jp.ac.kyoto_u.kuis.le4music.WavWriter;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.dvtrung.sound.lib.Utils.getFrameWaveform;

public class MainController implements Initializable {
    @FXML public BorderPane rootPane;
    @FXML private VBox chartPane;
    @FXML private Label statusLabel;
    @FXML private VBox checkBoxPane;
    @FXML private Slider posSlider;
    @FXML private Label fundamentalFrequencyLabel, loudnessLabel, vowelLabel, voicedLabel, noteLabel;
    @FXML private Label textFrameDuration;
    @FXML private Button btnPlay, btnPause, btnOpen, btnRecord;

    private WaveformChart waveformChart;
    private WaveformFrameChart waveformFrameChart;
    private SpectrumChart spectrumChart;
    private SpectrogramChart spectrogramChart;
    private AutocorrelationChart autocorrelationChart;
    private CepstrumChart cepstrumChart;
    //private RealCepstrumChart realCepstrumChart;
    private RecognitionResultChart recognitionResultChart;
    private LoudnessChart loudnessChart;
    private FrequencyChart frequencyChart;
    private ChromagramChart chromagramChart;
    private WaveformRChart waveformRChart;
    private SpectrogramRChart spectrogramRChart;

    private Options options;

    Recorder recorder;
    ExecutorService executor = Executors.newSingleThreadExecutor();

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

        options.setFrameDurationInMilis((int)(Le4MusicUtils.frameInterval * 1000));

        final Recorder.Builder builder = Recorder.builder();
        builder.sampleRate((float) options.sampleRate);
        builder.frameDuration(options.getFrameDuration());
        builder.frameSize(options.frameSize);
        try {
            recorder = builder.build();
            recorder.addAudioFrameListener((frame, position) -> executor.execute(() -> {
                options.waveform = frame;
                Platform.runLater(() -> {
                    options.recordedWaveform.add(frame);
                    plotRecording();
                });
            }));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        resetChart();

        openFile(new File("samples/test.wav"));
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
    private final void handleShowSeparationDlg(final ActionEvent ev) {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SourceSeparator.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Source Separator");
            stage.initOwner(((Node)ev.getSource()).getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private final void handlePlay(final ActionEvent ev) {
        player.start();
    }

    @FXML
    private final void handleRecord(final ActionEvent ev) {
        if (!options.recording) {
            options.recording = true;
            if (wavFile != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Close File");
                alert.setContentText("Do you want to close current file");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.NO) return;
                else {
                    wavFile = null;
                    options.newInstance();
                }
            }
            Platform.runLater(recorder::start);
        } else {
            options.recording = false;
            Platform.runLater(recorder::stop);
        }

        resetChart();

        // Update UI
        btnOpen.setDisable(options.recording);
        btnPlay.setDisable(options.recording);
        btnPause.setDisable(options.recording);
        btnRecord.setText(options.recording ? "Stop Recording" : "Record");

        for (SoundChart chart: arrChart) chart.onRecordingChanged(options.recording);
        handleChartToggleAction(null);
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
        updateUI();
        statusLabel.setText("Ready");
    }

    private void updateUI() {
        double[] wf = options.recording ? options.waveform : getFrameWaveform(options.waveform);

        fundamentalFrequencyLabel.setText(
                String.valueOf(Utils.getFundamentalFrequency(wf)) + "Hz");
        loudnessLabel.setText(Utils.getLoudness(wf) + "dB");
        vowelLabel.setText(TrainingHelper.getInstance().getLabel(wf, 0));
        voicedLabel.setText(Utils.isVoiced_ZeroCrossingRate(wf) ? "voiced" : "unvoiced");
        noteLabel.setText(Utils.f2note(Utils.getFundamentalFrequency(wf)));
    }

    private void plotRecording() {
        for (SoundChart chart: arrChart) {
            if (chart.getCheckBox().isSelected())
                chart.plot(options.waveform);
        }
        updateUI();
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
        arrChart.clear();

        if (options.recording) {
            waveformFrameChart = new WaveformFrameChart();
            arrChart.add(waveformFrameChart);

            waveformRChart = new WaveformRChart();
            arrChart.add(waveformRChart);

            cepstrumChart = new CepstrumChart();
            arrChart.add(cepstrumChart);

            loudnessChart = new LoudnessChart();
            frequencyChart = new FrequencyChart();

            arrChart.add(loudnessChart);
            arrChart.add(frequencyChart);

            spectrogramRChart = new SpectrogramRChart();
            arrChart.add(spectrogramRChart);

            chromagramChart = new ChromagramChart();
            arrChart.add(chromagramChart);
        } else {
            waveformChart = new WaveformChart();
            arrChart.add(waveformChart);

            waveformFrameChart = new WaveformFrameChart();
            arrChart.add(waveformFrameChart);

            autocorrelationChart = new AutocorrelationChart();
            arrChart.add(autocorrelationChart);

            spectrumChart = new SpectrumChart();
            arrChart.add(spectrumChart);

            cepstrumChart = new CepstrumChart();
            arrChart.add(cepstrumChart);

            spectrogramChart = new SpectrogramChart();
            arrChart.add(spectrogramChart);

            recognitionResultChart = new RecognitionResultChart();
            arrChart.add(recognitionResultChart);
        }

        checkBoxPane.getChildren().clear();
        for (SoundChart chart: arrChart) {
            chart.init();
            chart.getCheckBox().setOnAction(e -> handleChartToggleAction(e));
            checkBoxPane.getChildren().add(chart.getCheckBox());
        }

        //waveformChart.getCheckBox().setSelected(true);
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

    @FXML
    public void handleShowKaraokeDlg(ActionEvent actionEvent) {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Karaoke.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Karaoke");
            stage.initOwner(((Node)actionEvent.getSource()).getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleShowVoiceRecognitionDlg(ActionEvent actionEvent) {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/VoiceRecognition.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Voice Recognition");
            stage.initOwner(((Node)actionEvent.getSource()).getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleSave(ActionEvent actionEvent) {
        if (options.recordedWaveform.size() == 0) return;

        try {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save WAV File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Wav Files", "*.wav"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            wavFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
            if (wavFile == null) return;
            else {
                WavWriter wavWriter = WavWriter.newWavWriter(wavFile, options.sampleRate);
                for (double[] wf: options.recordedWaveform) wavWriter.append(wf);
                wavWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
