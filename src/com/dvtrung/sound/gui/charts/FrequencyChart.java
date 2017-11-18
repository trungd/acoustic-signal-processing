package com.dvtrung.sound.gui.charts;

import com.dvtrung.sound.lib.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FrequencyChart extends LineChart<Number, Number> {
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private static int FRAME_COUNT = 200;

    double[][] freqs = new double[2][FRAME_COUNT];

    int initCount = 0;

    public FrequencyChart() {
        super(new NumberAxis(), new NumberAxis());

        xAxis = (NumberAxis) getXAxis();
        xAxis.setLabel("Time (s)");

        yAxis = (NumberAxis)getYAxis();
        yAxis.setLabel("Note Number");

        VBox.setVgrow(this, Priority.ALWAYS);
        setAnimated(false);
        setCreateSymbols(false);
    }

    public void addWaveform(double[] wf) {
        //if (Utils.isVoiced_ZeroCrossingRate(wf)) fr = Utils.f2noteId(Utils.getFundamentalFrequencySHS(wf));
        //else fr = 0;

        int f1 = Utils.getFundamentalFrequency(wf);
        int f2 = Utils.f2noteId(Utils.getFundamentalFrequencySHS(wf));

        //if (!Utils.isVoiced_Correlation(wf, f2)) f2 = 0;

        int n1 = Utils.f2noteId(f1);
        int n2 = Utils.f2noteId(f2);

        if (initCount < FRAME_COUNT) {
            freqs[0][initCount] = n1;
            freqs[1][initCount] = n2;
            initCount++;
        } else {
            for (int i = 1; i < FRAME_COUNT; i++) freqs[0][i - 1] = freqs[0][i];
            for (int i = 1; i < FRAME_COUNT; i++) freqs[1][i - 1] = freqs[1][i];
            freqs[0][FRAME_COUNT - 1] = n1;
            freqs[1][FRAME_COUNT - 1] = n2;
        }

        ObservableList<XYChart.Data<Number, Number>>  data_freq1 = IntStream.range(0, freqs[0].length)
                .mapToObj(i -> new XYChart.Data<Number, Number>(i, freqs[0][i]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        final XYChart.Series<Number, Number> series_freq1 = new XYChart.Series<>("Autocorrelation", data_freq1);

        ObservableList<XYChart.Data<Number, Number>>  data_freq2 = IntStream.range(0, freqs[1].length)
                .mapToObj(i -> new XYChart.Data<Number, Number>(i, freqs[1][i]))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        final XYChart.Series<Number, Number> series_freq2 = new XYChart.Series<>("SHS", data_freq2);

        getData().clear();
        getData().add(series_freq1);
        getData().add(series_freq2);

        yAxis.setAutoRanging(false);
        yAxis.setTickUnit(10);
        yAxis.setMinorTickCount(1);

        yAxis.setLowerBound(30);
        yAxis.setUpperBound(80);

        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(10);
        xAxis.setMinorTickCount(1);
        xAxis.setUpperBound(FRAME_COUNT);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();

        if (getData().size() == 0) return;

        for (int k = 0; k < 0; k++) {
            Path p = (Path)getData().get(k).getNode();

            IntStream.range(0, freqs[0].length - 1).mapToObj(i -> freqs[0].length - i - 1).forEach(i -> {
                Data<Number, Number> d1 = getData().get(0).getData().get(i);
                Data<Number, Number> d2 = getData().get(0).getData().get(i + 1);
                if (d1.getYValue().intValue() - d2.getYValue().intValue() > 10)
                    p.getElements().add(i,
                            new MoveTo(
                                    getXAxis().getDisplayPosition(d2.getXValue().intValue() + 1),
                                    getYAxis().getDisplayPosition(d2.getYValue())
                            ));

            });
        }
    }
}
