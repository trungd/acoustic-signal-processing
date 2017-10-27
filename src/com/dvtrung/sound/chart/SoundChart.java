package com.dvtrung.sound.chart;

import com.dvtrung.sound.Options;
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

    // Get waveform of the frame starting from current position
    double[] getFrameWaveform(double[] waveform) {
        int length = Math.min(Options.getInstance().frameSize, waveform.length - (int)Options.getInstance().currentPos);
        double[] wf = new double[length];
        System.arraycopy(waveform, (int)Options.getInstance().currentPos, wf, 0, length);
        return wf;
    }
}