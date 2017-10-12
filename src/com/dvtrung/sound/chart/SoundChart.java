package com.dvtrung.sound.chart;

import javafx.scene.chart.Chart;
import javafx.scene.control.CheckBox;

public class SoundChart {
    private CheckBox checkBox;

    public void init() {};
    public void plot(double[] waveform, double sampleRate) {};
    public Chart getChart() { return null; };
    public String getTitle() { return null; };

    public CheckBox getCheckBox() {
        if (checkBox == null) {
            checkBox = new CheckBox();
            checkBox.setText(getTitle());
        }
        return checkBox;
    }
}