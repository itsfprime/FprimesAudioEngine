import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SoundGraphPanel extends JPanel {
	private BufferedImage graphImage;

	public SoundGraphPanel() {
		setPreferredSize(new Dimension(500, 300));
		graphImage = new BufferedImage(500, 300, BufferedImage.TYPE_INT_ARGB);
	}

	public void updateGraph(byte[] soundData) {
		int width = graphImage.getWidth();
		int height = graphImage.getHeight();
		Graphics2D g2d = graphImage.createGraphics();
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);

		g2d.setColor(Color.GREEN);
		for (int i = 0; i < soundData.length - 1; i++) {
			int x1 = i * width / soundData.length;
			int y1 = height / 2 + (soundData[i] * height / 256);
			int x2 = (i + 1) * width / soundData.length;
			int y2 = height / 2 + (soundData[i + 1] * height / 256);
			g2d.drawLine(x1, y1, x2, y2);
		}

		g2d.dispose();
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(graphImage, 0, 0, null);
	}
}