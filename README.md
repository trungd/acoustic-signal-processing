# acoustic-signal-processing

Voice/Music acoustic signal processing techniques with explicative GUI.

# Implementations

- [ ] Matrix and Vector Manipulations

- [ ] Hidden Markov Model with MFCC as feature vector for simple voice recognition

- [x] Non-negative Matrix Factorization for Sound Separation 

- [x] Autocorrelation / Subharmonic Summation for pitch detection

# Features

- [x] Real-time or file input

- [x] Display multiple charts as of entire period or specified frame length.

- [x] Display calculation and recognition results: loudness, fundamental frequency, Japanese vowel prediction,...

# Chart List

## Waveform (current frame / entire period)

Waveform of entire period with marker indicating current position.

Waveform of current frame.

## Spectrum

## Spectrogram

Spectrogram with display of voiced / unvoiced period differentiated using zero-crossing rate and fundamental frequency.

## Autocorrelation

Autocorrelation and fundamental frequency position (second peak)

## Spectrum + Cepstrum (frame)

Display filter spectrum (default: lifter order 13) in comparison with original spectrum.

## Recognition Result

Using saved training result to determine a / i / u / e / o period.

# Libraries

- [Commons Math: The Apache Commons Mathematics Library](http://commons.apache.org/proper/commons-math/)

- [Commons CLI](http://commons.apache.org/proper/commons-cli/)

- [Utilities Library (for class use)](http://winnie.kuis.kyoto-u.ac.jp/~itoyama/le4-music/lib/le4music.jar)
