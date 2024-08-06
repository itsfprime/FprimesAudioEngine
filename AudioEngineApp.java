import javax.swing.*;

public class AudioEngineApp {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			AudioEngineController controller = new AudioEngineController();
			AudioEngineGUI gui = new AudioEngineGUI(controller);
			gui.createAndShowGUI();
		});
	}
}