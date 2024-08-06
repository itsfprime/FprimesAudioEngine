import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AudioEngineGUI {
	private AudioEngineController controller;
	private JFrame mainWindow;
	private JPanel mainPanel;

	public AudioEngineGUI(AudioEngineController controller) {
		this.controller = controller;
	}

	public void createAndShowGUI() {
		mainWindow = new JFrame("Audio Engine Ver2.0");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setLocation(700, 300);
		mainWindow.setMinimumSize(new Dimension(500, 300));
		mainWindow.setMaximumSize(new Dimension(1000, 1000));

		mainPanel = new JPanel(new BorderLayout());
		JPanel slidersPanel = createSlidersPanel();
		JPanel buttonPanel = createButtonPanel();

		mainPanel.add(slidersPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainWindow.add(mainPanel);
		mainWindow.pack();
		mainWindow.setVisible(true);
	}

	private JPanel createSlidersPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
		panel.setBackground(Color.GRAY);

		SliderComponent volumeSlider = new SliderComponent("Volume", 0, 10, 5, Color.BLUE, 5, new int[]{0, 5, 10, 15, 20});
		SliderComponent frequencySlider = new SliderComponent("Frequency", 0, 20000, 5000, Color.GREEN, 5000, new int[]{0, 5000, 10000, 15000, 20000});
		SliderComponent phaseSlider = new SliderComponent("Phase", 0, 360, 0, Color.MAGENTA, 90, new int[]{0, 90, 180, 270, 360});

		panel.add(volumeSlider.getLabel());
		panel.add(volumeSlider.getSlider());
		panel.add(volumeSlider.getTextField());

		panel.add(frequencySlider.getLabel());
		panel.add(frequencySlider.getSlider());
		panel.add(frequencySlider.getTextField());

		panel.add(phaseSlider.getLabel());
		panel.add(phaseSlider.getSlider());
		panel.add(phaseSlider.getTextField());

		// Add listeners to update values in the controller
		controller.addSlider(volumeSlider, frequencySlider, phaseSlider);

		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(e -> controller.handleSubmit());

		JButton holdToPlayButton = new JButton("Hold to Play");
		holdToPlayButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				controller.handleHoldToPlay(true);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				controller.handleHoldToPlay(false);
			}
		});

		panel.add(submitButton);
		panel.add(holdToPlayButton);
		return panel;
	}
}