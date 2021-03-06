package com.dvtrung.sound.gui.charts.controllers;

import com.dvtrung.sound.lib.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpectrumChart extends SoundChart {
    private LineChart spectrumChart;
    NumberAxis xAxis, yAxis;
    double[] logSpec;

    @Override
    public void init() {
        super.init();

        xAxis = new NumberAxis();
        xAxis.setLabel("Frequency (Hz)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Amplitude (dB)");

        /*
        final DoubleProperty xLo = xAxis.lowerBoundProperty();
        final DoubleProperty xUp = xAxis.upperBoundProperty();
        xAxis.tickUnitProperty().bind(new DoubleBinding() {
            {
                super.bind(xLo, xUp);
            }
            @Override
            protected double computeValue() {
                return Le4MusicUtils.autoTickUnit(xUp.get() - xLo.get());
            }
        });

        final DoubleProperty yLo = yAxis.lowerBoundProperty();
        final DoubleProperty yUp = yAxis.upperBoundProperty();
        yAxis.tickUnitProperty().bind(new DoubleBinding() {
            {
                super.bind(yLo, yUp);
            }
            @Override
            protected double computeValue() {
                return Le4MusicUtils.autoTickUnit(yUp.get() - yLo.get());
            }
        });

        final double magnifyScale = 2.0;
        xAxis.setOnScroll(ev -> {
            final double center = xAxis.getValueForDisplay(ev.getX()).doubleValue();
            final double factor = (ev.getDeltaY() < 0.0) ? magnifyScale : 1.0 / magnifyScale;
            xAxis.setLowerBound(center - (center - xAxis.getLowerBound()) * factor);
            xAxis.setUpperBound(center + (xAxis.getUpperBound() - center) * factor);
        });
        yAxis.setOnScroll(ev -> {
            final double center = yAxis.getValueForDisplay(ev.getY()).doubleValue();
            final double factor = (ev.getDeltaY() < 0.0) ? magnifyScale : 1.0 / magnifyScale;
            yAxis.setLowerBound(center - (center - yAxis.getLowerBound()) * factor);
            yAxis.setUpperBound(center + (yAxis.getUpperBound() - center) * factor);
        });
        */

        spectrumChart = new LineChart<Number, Number>(xAxis, yAxis);
        spectrumChart.setTitle("Spectrum");
        spectrumChart.setCreateSymbols(false);
        spectrumChart.setAnimated(false);
        VBox.setVgrow(spectrumChart, Priority.ALWAYS);
    }

    @Override
    public void plot(double[] wf) {
        if (logSpec != null && !options.recording) return;
        final int fftSize = 1 << Le4MusicUtils.nextPow2(wf.length);
        logSpec = Utils.getLogSpectrum(wf);

        final ObservableList<XYChart.Data<Number, Number>> data = IntStream.range(0, logSpec.length)
                .mapToObj(i -> new XYChart.Data<Number, Number>(i * options.sampleRate / fftSize, logSpec[i]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        final XYChart.Series<Number, Number> series = new XYChart.Series<>("Spectrum", data);

        spectrumChart.getData().clear();
        spectrumChart.getData().clear();
        spectrumChart.getData().add(series);
    }

    @Override
    public void onRecordingChanged(boolean recording) {

    }

    @Override
    public Chart getChart() {
        return spectrumChart;
    }
    public String getTitle() { return "Spectrum"; }
}
