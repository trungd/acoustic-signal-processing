package com.dvtrung.sound;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public final class TrainCLI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String[] args = getParameters().getRaw().toArray(new String[0]);
        Train.train(args[0]);
        Platform.exit();
    }
}