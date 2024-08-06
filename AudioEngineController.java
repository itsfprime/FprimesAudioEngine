import javax.sound.sampled.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioEngineController {
	private List<SliderComponent> sliders;
	private SourceDataLine line;
	private Thread playThread;
	private AtomicBoolean isPlaying;

	public AudioEngineController() {
		sliders = new ArrayList<>();
		isPlaying = new AtomicBoolean(false);
	}

	public void addSlider(SliderComponent... sliderComponents) {
		for (SliderComponent sliderComponent : sliderComponents) {
			sliders.add(sliderComponent);
		}
	}

	public void handleSubmit() {
		double[] values = sliders.stream()
				.mapToDouble(sliderComponent -> Double.parseDouble(sliderComponent.getTextField().getText()))
				.toArray();
		soundGenerator(values);
	}

	public void handleHoldToPlay(boolean play) {
		if (play) {
			isPlaying.set(true);
			double[] values = sliders.stream()
					.mapToDouble(sliderComponent -> Double.parseDouble(sliderComponent.getTextField().getText()))
					.toArray();
			playThread = new Thread(() -> soundGeneratorContinuous(values));
			playThread.start();
		} else {
			isPlaying.set(false);
			stopSound();
		}
	}

	public void soundGenerator(double[] values) {
		double volume = values[0];
		double frequency = values[1];
		double phase = values[2];

		final int SAMPLE_RATE = 44100;
		byte[] buffer = new byte[SAMPLE_RATE];
		AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);

		double volumeScale = volume / 20.0; // Convert volume to a range of 0.0 to 1.0
		double phaseShift = Math.toRadians(phase); // Convert phase to radians

		try {
			SourceDataLine line = AudioSystem.getSourceDataLine(format);
			line.open(format);
			line.start();

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

	private void soundGeneratorContinuous(double[] values) {
		double volume = values[0];
		double frequency = values[1];
		double phase = values[2];

		final int SAMPLE_RATE = 44100;
		byte[] buffer = new byte[SAMPLE_RATE];
		AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);

		double volumeScale = volume / 20.0; // Convert volume to a range of 0.0 to 1.0
		double phaseShift = Math.toRadians(phase); // Convert phase to radians

		try {
			line = AudioSystem.getSourceDataLine(format);
			line.open(format);
			line.start();

			while (isPlaying.get()) {
				for (int i = 0; i < buffer.length; i++) {
					double angle = (i / (SAMPLE_RATE / frequency) * 2.0 * Math.PI) + phaseShift;
					buffer[i] = (byte) (Math.sin(angle) * 127f * volumeScale);
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