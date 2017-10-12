package com.dvtrung.sound.chart;

import javafx.scene.chart.*;

public class LoudnessChart extends SoundChart {
    private BarChart loudnessChart;
    private int loudness;

    @Override
    public void init() {
        super.init();

        final CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (s)");
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Loudness (dB)");

        loudnessChart = new BarChart<String, Number>(xAxis, yAxis);
        loudnessChart.setTitle("Loudness");
        loudnessChart.setBarGap(0);
        loudnessChart.setCategoryGap(0);
        loudnessChart.setAnimated(false);
    }

    @Override
    public void plot(double[] waveform, double sampleRate) {
        final int step = 10;
        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < waveform.length; i += step) {
            int j = Math.min(waveform.length - 1, i + step - 1);
            double sum = 0;
            for (int k = i; k <= j; k++) sum += Math.pow(waveform[k], 2);
            series.getData().add(new XYChart.Data(String.valueOf(i), 20 * Math.log10(sum / (j - i + 1))));
        }
        loudnessChart.getData().clear();
        loudnessChart.getData().add(series);

        // Calculate loudness
        double sum = 0;
        for (int k = 0; k < waveform.length; k++) sum += Math.pow(waveform[k], 2);
        loudness = (int)(20 * Math.log10(sum / waveform.length));
    }

    @Override
    public Chart getChart() {
        return loudnessChart;
    }

    @Override
    public String getTitle() { return "Loudness"; }

    public int getLoudness() {
        return loudness;
    }
}
