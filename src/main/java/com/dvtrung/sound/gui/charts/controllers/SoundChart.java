package com.dvtrung.sound.gui.charts.controllers;

import com.dvtrung.sound.gui.Options;
import javafx.scene.chart.Chart;
import javafx.scene.control.CheckBox;

/**
 * Base class for charts
 */
public class SoundChart {
    private CheckBox checkBox;

    public void init() {};

    public void plot(double[] waveform) {};

    public Chart getChart() { return null; };

    public String getTitle() { return null; };

    public Options options = Options.getInstance();

    // Get checkbox to show/hide chart
    public CheckBox getCheckBox() {
        if (checkBox == null) {
            checkBox = new CheckBox();
            checkBox.setText(getTitle());
        }
        return checkBox;
    }

    public void onRecordingChanged(boolean recording) {
        
    }
}