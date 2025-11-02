import javax.swing.*;

public class AudioEngineApp {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			AudioEngineController controller = new AudioEngineController();
			AudioEngineGui gui = new AudioEngineGui(controller);
			gui.createWindow();
		});
	}
}