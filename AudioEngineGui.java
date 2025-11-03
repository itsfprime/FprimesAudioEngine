import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AudioEngineGui {
    private AudioEngineController controller;
    private JFrame mainWindow;
    private SliderComponent masterVolumeSlider;
    private SliderComponent harmonicSlider;
    private final Dimension size = new Dimension(700, 700);
    public JPanel masterHolderPanel;
    public GridLayout masterGridLayout;

    public AudioEngineGui(AudioEngineController controller) {
        this.controller = controller;
    }

    public void createWindow(){
        mainWindow = new JFrame("Audio Engine Ver2.0");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setLocation(700, 300);
        mainWindow.setSize(size);
        masterHolderPanel = createMasterHolderPanel();
        mainWindow.add(masterHolderPanel);

        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    public void refreshWindow(){
        mainWindow.setVisible(false);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    public JPanel createMasterHolderPanel(){
        masterGridLayout = new GridLayout(10, 3, 10, 10);
        JPanel panel = new JPanel(masterGridLayout);

        masterVolumeSlider = new SliderComponent("Volume", 0, 10, 3, Color.BLUE, 1, new int[]{0, 5, 10, 15, 20});
        harmonicSlider = new SliderComponent("Harmonics", 0, 64, 0, Color.orange, 1, new int[]{0, 32, 64});

        panel.add(masterVolumeSlider.getLabel());
        panel.add(masterVolumeSlider.getSlider());
        panel.add(masterVolumeSlider.getTextField());

        panel.add(harmonicSlider.getLabel());
        panel.add(harmonicSlider.getSlider());
        panel.add(harmonicSlider.getTextField());

        JPanel buttonsPanel = createButtonsPanel();
        panel.add(buttonsPanel);

        return panel;
    }

    public void createSlidersPanel(){
        JPanel panel = new JPanel();
        panel.setBackground(Color.GRAY);

        SliderComponent frequencySlider = new SliderComponent("Note", 0, 12, 0, Color.GREEN, 1, new int[]{0, 2, 3, 5, 7, 8, 10, 12});
        SliderComponent octaveSlider = new SliderComponent("Octave", 0, 8, 4, Color.RED, 1, new int[]{0, 4, 8});


        panel.add(frequencySlider.getLabel());
        panel.add(frequencySlider.getSlider());
        panel.add(frequencySlider.getTextField());

        panel.add(octaveSlider.getLabel());
        panel.add(octaveSlider.getSlider());
        panel.add(octaveSlider.getTextField());

        controller.addSlider(frequencySlider, octaveSlider);
        panel.setVisible(true);

        masterHolderPanel.add(panel);
        refreshWindow();
    }

    public JPanel createButtonsPanel(){
        JPanel panel = new JPanel(new FlowLayout());

        JButton holdToPlayButton = new JButton("Hold to Play");
        JButton addNewButton = new JButton("Add New Voice");
        JButton playButton = new JButton("Play");
        JButton showVisualizerButton = new JButton("Show Visualizer");
        JButton SaveWaveButton = new JButton("Save Sound");
        JComboBox waveTypeBox = new JComboBox(new String[]{"Sine", "Square", "Triangle", "Sawtooth"});
        JTextField bassField = new JTextField("Bass", 5);
        JTextField midField = new JTextField("Mid", 5);
        JTextField trebleField = new JTextField("Treble", 5);

        JPanel EQPanel = new JPanel(new GridLayout(3, 2));


        bassField = new JTextField("1.0", 5);
        midField = new JTextField("1.0", 5);
        trebleField = new JTextField("1.0", 5);

        JTextField finalBassField = bassField;
        JTextField finalMidField = midField;
        JTextField finalTrebleField = trebleField;

        bassField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setEQ(Float.parseFloat(finalBassField.getText()), Float.parseFloat(finalMidField.getText()), Float.parseFloat(finalTrebleField.getText()));
            }
        });

        midField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setEQ(Float.parseFloat(finalBassField.getText()), Float.parseFloat(finalMidField.getText()), Float.parseFloat(finalTrebleField.getText()));
            }
        });

        trebleField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setEQ(Float.parseFloat(finalBassField.getText()), Float.parseFloat(finalMidField.getText()), Float.parseFloat(finalTrebleField.getText()));
            }
        });

        EQPanel.add(new JLabel("Bass"));
        EQPanel.add(bassField);
        EQPanel.add(new JLabel("Mid"));
        EQPanel.add(midField);
        EQPanel.add(new JLabel("Treble"));
        EQPanel.add(trebleField);

        waveTypeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int waveType = waveTypeBox.getSelectedIndex();
                controller.handleWaveTypeChange(waveType);
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.handlePlayButton(Integer.parseInt(masterVolumeSlider.getTextField().getText()));
            }
        });

        addNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createSlidersPanel();
            }
        });

        holdToPlayButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                controller.handleHoldToPlay(true, Integer.parseInt(masterVolumeSlider.getTextField().getText()), Integer.parseInt(harmonicSlider.getTextField().getText()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                controller.handleHoldToPlay(false, Integer.parseInt(masterVolumeSlider.getTextField().getText()), Integer.parseInt(harmonicSlider.getTextField().getText()));
            }
        });

        panel.add(EQPanel);
        panel.add(waveTypeBox);
        panel.add(SaveWaveButton);
        panel.add(showVisualizerButton);
        panel.add(addNewButton);
        panel.add(holdToPlayButton);
        panel.add(playButton);

        return panel;
    }
}
