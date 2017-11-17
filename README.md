# acoustic-signal-processing

# Libraries

- [Commons Math: The Apache Commons Mathematics Library](http://commons.apache.org/proper/commons-math/)

- [Commons CLI](http://commons.apache.org/proper/commons-cli/)

- [Utilities Library (for class use)](http://winnie.kuis.kyoto-u.ac.jp/~itoyama/le4-music/lib/le4music.jar)

# Features

- Read data series from .wav file and display several charts in entire period or defined frame length.

- Calculate loudness, fundamental frequency and make prediction on which Japanese vowel sounded.

- Separate source component (spectral envelop) and filter component.

# Chart List

## Waveform

Waveform of entire period with marker indicating current position.

## Waveform (frame)

Waveform of current frame.

## Spectrum

## Spectrogram

Spectrogram with display of voiced / unvoiced period differentiated using zero-crossing rate and fundamental frequency.

## Autocorrelation

Autocorrelation and fundamental frequency position (second peak)

## Spectrum + Cepstrum (frame)

Display filter spectrum (default: lifter order 13) in comparison with original spectrum.

## Real Cepstrum

Source spectrum

## Recognition Result

Using saved training result to determine a / i / u / e / o period.
