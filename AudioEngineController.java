import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioEngineController {
    private List<SliderComponent> sliders;
    private static SourceDataLine line;
    private Thread playThread;
    private final AtomicBoolean isPlaying;
    public static int SAMPLE_RATE = 44100;
    public static boolean isSaw = false;
    public static boolean isSquare = false;
    public static boolean isTriangle = false;
    public static boolean isSine = false;
    private final int BASS_UPPER_BOUND = 200;
    private final int MID_UPPER_BOUND = 2000;
    private double bass = 1.0;
    private double mid = 1.0;
    private double treble = 1.0;

    public AudioEngineController() {
        sliders = new ArrayList<>();
        isPlaying = new AtomicBoolean(false);
        isSine = true;
    }

    private double[] getSliderValues(){
        return sliders.stream()
                .mapToDouble(sliderComponent -> Double.parseDouble(sliderComponent.getTextField().getText()))
                .toArray();
    }

    public void addSlider(SliderComponent... sliderComponents) {
        sliders.addAll(Arrays.asList(sliderComponents));
    }
    public void handlePlayButton(int volume) {
        new Thread(() -> FourierSoundGenerator(getSliderValues(), volume)).start();
    }
    private void FourierSoundGenerator(double[] values, int vol) {
        int totalVoices = values.length / 2;

        double[] frequencies = new double[totalVoices];
        for (int i = 0; i < values.length; i += 2) {
            frequencies[i / 2] = calculateFrequency(values[i], values[i + 1]);
        }

        double[] phases = new double[frequencies.length];

        double[] phaseIncrementations = new double[frequencies.length];
        for (int i = 0; i < phaseIncrementations.length; i++) {
            phaseIncrementations[i] = incrementPhase(frequencies[i]);
        }

        byte[] buffer = new byte[SAMPLE_RATE];
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);

        double volumeScale = (double) vol / 20.0;

        // Low-pass filter setup
        int filterOrder = 5;  // Number of coefficients for the FIR filter
        double[] filterCoefficients = new double[filterOrder];
        // Simple moving average filter
        Arrays.fill(filterCoefficients, 1.0 / filterOrder);
        double[] filterBuffer = new double[filterOrder];

        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            for (int i = 0; i < SAMPLE_RATE; i++) {
                double signal = 0;

                for (int k = 0; k < frequencies.length; k++) {
                    signal += Math.sin(phases[k]) * 127f * volumeScale;
                }

                // Apply exponential decay
                signal *= Math.exp(-3.0 * i / SAMPLE_RATE);

                // Apply low-pass FIR filter
                filterBuffer[i % filterOrder] = signal;
                double filteredSignal = 0;
                for (int j = 0; j < filterOrder; j++) {
                    filteredSignal += filterCoefficients[j] * filterBuffer[(i - j + filterOrder) % filterOrder];
                }

                buffer[i] = (byte) Math.max(Math.min(filteredSignal, 127), -127);

                for (int k = 0; k < phases.length; k++) {
                    phases[k] += phaseIncrementations[k];
                    if (phases[k] > 2.0 * Math.PI) {
                        phases[k] -= 2.0 * Math.PI;
                    }
                }
            }

            line.write(buffer, 0, buffer.length);
            line.drain();
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void handleHoldToPlay(boolean play, int volume, int harmonics) {
        if (play) {
            isPlaying.set(true);

            playThread = new Thread(() -> soundGeneratorContinuous(getSliderValues(), volume, harmonics));
            playThread.start();

        } else {
            isPlaying.set(false);
            stopSound();
        }
    }
    private static double incrementPhase(double frequency){
        return (2.0 * Math.PI * frequency) / SAMPLE_RATE;
    }
    private static double calculateFrequency(double noteValue, double octave){
        return (440 * Math.pow(2, (noteValue) / 12) / 4 * Math.pow(2, octave - 1));
    }
    public double findHarmonic(double n, double frequency) {
        return Math.sin(frequency / n);
    }
    public void handleWaveTypeChange(int waveType){
        isSine = false;
        isTriangle = false;
        isSquare = false;
        isSaw = false;

        switch (waveType) {
            case 0:
                isSine = true;
                break;
            case 1:
                isSquare = true;
                break;
            case 2:
                isTriangle = true;
                break;
            case 3:
                isSaw = true;
                break;
            default:
                break;
        }
    }
    public static void soundGenerator(double noteValue, double octave, int vol, double time){
        int totalSampleTime = (int)((double)SAMPLE_RATE * time);
        byte[] buffer = new byte[totalSampleTime];
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        double frequency = calculateFrequency(noteValue, octave);
        double volumeScale = vol / 20.0;
        double phaseShift;

        try {
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            phaseShift = incrementPhase(frequency);

            for (int i = 0; i < buffer.length; i++) {
                double angle = (i / (SAMPLE_RATE / frequency) * 2.0 * Math.PI) + phaseShift;
                buffer[i] = (byte) (Math.sin(angle) * 127f * volumeScale);
            }

            line.write(buffer, 0, buffer.length);
            line.drain();
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void setEQ(double bass, double mid, double treble) {
        this.bass = bass;
        this.mid = mid;
        this.treble = treble;
    }
    public void soundGeneratorContinuous(double[] values, int vol, int harmonics) {
        int totalVoices = values.length / 2;

        // Ensure values array has an even number of elements
        if (values.length % 2 != 0) {
            System.err.println("Values array length is not even, which indicates a missing note or octave value.");
            return;
        }

        double[] frequencies = new double[totalVoices];
        for (int i = 0; i < values.length; i+=2){
            // Guard against out-of-bounds access
            if (i + 1 < values.length) {
                frequencies[i / 2] = calculateFrequency(values[i], values[i + 1]);
            } else {
                System.err.println("Unexpected end of values array.");
                return;
            }
        }

        double[] phases = new double[frequencies.length];

        double[] phaseIncrementations = new double[frequencies.length];
        for (int i = 0; i < phaseIncrementations.length; i++){
            phaseIncrementations[i] = incrementPhase(frequencies[i]);
        }

        byte[] buffer = new byte[SAMPLE_RATE];
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);

        double volumeScale = (double) vol / 20.0; // Convert volume to a range of 0.0 to 1.0

        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            while (isPlaying.get()) {
                for (int i = 0; i < buffer.length; i++) {
                    double signal = 0.0;

                    for (int k = 0; k < frequencies.length; k++){
                        double baseSignal = Math.sin(phases[k]) * 127f * volumeScale;
                        if (isSine){
                            if (frequencies[k] < BASS_UPPER_BOUND)
                                signal += baseSignal * bass;

                            else if(frequencies[k] < MID_UPPER_BOUND)
                                signal += baseSignal * mid;

                            else signal += baseSignal * treble;

                        }
                        else if (isSaw){
                            double t = (double) i / SAMPLE_RATE;
                            baseSignal += 2.0 * (t * frequencies[k] - Math.floor(0.5 + t * frequencies[k])) * 127.0 * volumeScale;
                            if (frequencies[k] < BASS_UPPER_BOUND)
                                signal += baseSignal * bass;

                            else if(frequencies[k] < MID_UPPER_BOUND)
                                signal += baseSignal * mid;

                            else
                                signal += baseSignal * treble;

                        }
                        else if (isSquare){
                            baseSignal += Math.signum(Math.sin(phases[k])) * 127.0 * volumeScale;
                            if (frequencies[k] < BASS_UPPER_BOUND)
                                signal += baseSignal * bass;

                            else if(frequencies[k] < MID_UPPER_BOUND)
                                signal += baseSignal * mid;

                            else
                                signal += baseSignal * treble;

                        }
                        else if (isTriangle){
                            double phase = phases[k] / (2.0 * Math.PI); // Normalize phase to range [0, 1]
                            double triangleWave = 2.0 * Math.abs(2.0 * (phase - Math.floor(phase + 0.5))) - 1.0;

                            baseSignal += triangleWave * 127.0 * volumeScale;

                            if (frequencies[k] < BASS_UPPER_BOUND)
                                signal += baseSignal * bass;

                            else if(frequencies[k] < MID_UPPER_BOUND)
                                signal += baseSignal * mid;

                            else
                                signal += baseSignal * treble;


                            // Update phase for the next sample
                            phases[k] += phaseIncrementations[k];
                            if (phases[k] > 2.0 * Math.PI)
                                phases[k] -= 2.0 * Math.PI;
                        }
                    }

                    // Ensure signal remains within byte range
                    buffer[i] = (byte) Math.max(Math.min(signal, 127), -127);

                    // Update phases for the next sample, wrap phase to stay within 0 to 2π if necessary
                    for (int k = 0; k < phases.length; k++){
                        phases[k] += phaseIncrementations[k];
                        if (phases[k] > 2.0 * Math.PI){
                            phases[k] -= 2.0 * Math.PI;
                        }
                    }
                }

                line.write(buffer, 0, buffer.length);
            }

            line.drain();
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    private void stopSound() {
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
        }
        if (playThread != null && playThread.isAlive()) {
            playThread.interrupt();
        }
    }
}