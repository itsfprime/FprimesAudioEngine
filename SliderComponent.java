import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.util.Hashtable;

public class SliderComponent extends Component {
	private JLabel label;
	private JSlider slider;
	private JTextField textField;

	public SliderComponent(String name, int min, int max, int value, Color color, int majorTickSpacing, int[] labels) {
		label = new JLabel(name);
		slider = createSlider(min, max, value, color, majorTickSpacing, labels);
		textField = createTextField(String.valueOf(value));

		// Add change listeners to update text field
		addSliderChangeListener(slider, textField);

		// Add action listener to update slider
		addTextFieldActionListener(textField, slider);
	}

	public JLabel getLabel() {
		return label;
	}

	public JSlider getSlider() {
		return slider;
	}

	public JTextField getTextField() {
		return textField;
	}

	private JSlider createSlider(int min, int max, int value, Color color, int majorTickSpacing, int[] labels) {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
		slider.setMajorTickSpacing(majorTickSpacing);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setForeground(color);

		slider.setUI(new BasicSliderUI(slider) {
			@Override
			public void paintTrack(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(color);
				g2d.fillRoundRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height, 1, 1);
			}
		});

		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		for (int label : labels) {
			labelTable.put(label, new JLabel(String.valueOf(label)));
		}
		slider.setLabelTable(labelTable);

		return slider;
	}

	private JTextField createTextField(String text) {
		JTextField textField = new JTextField(text, 5);
		textField.setFont(new Font("Arial", Font.PLAIN, 14));
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		textField.setBackground(Color.LIGHT_GRAY);
		return textField;
	}

	private void addSliderChangeListener(JSlider slider, JTextField textField) {
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				textField.setText(String.valueOf(source.getValue()));
			}
		});
	}

	private void addTextFieldActionListener(JTextField textField, JSlider slider) {
		textField.addActionListener(e -> {
			try {
				int value = Integer.parseInt(textField.getText());
				slider.setValue(value);
			} catch (NumberFormatException ex) {
				textField.setText(String.valueOf(slider.getValue()));
			}
		});
	}
}