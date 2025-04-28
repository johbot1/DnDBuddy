import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        // --- Frame Setup (Phase 1) ---
        JFrame frame = new JFrame("DM Buddy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // --- Script Viewer (Center) ---
        JTextPane scriptViewer = new JTextPane();
        scriptViewer.setEditable(false);
        scriptViewer.setText("Adventure script will appear here…");
        scriptViewer.setMargin(new Insets(10, 10, 10, 10));
        frame.add(new JScrollPane(scriptViewer), BorderLayout.CENTER);

        // --- Controls Pane (East) ---
        JPanel controlsPane = new JPanel();
        controlsPane.setPreferredSize(new Dimension(250, 0));
        controlsPane.setLayout(null);  // Absolute positioning
        frame.add(controlsPane, BorderLayout.EAST);

        // Scene/Act Label
        JLabel sceneLabel = new JLabel("Scene X, Act 1");
        sceneLabel.setFont(sceneLabel.getFont().deriveFont(Font.BOLD, 14f));
        sceneLabel.setBounds(10, 10, 230, 25);
        controlsPane.add(sceneLabel);

        // Progress Slider (music playback position)
        JSlider progressSlider = new JSlider(0, 100, 0);
        progressSlider.setBounds(10, 40, 230, 20);
        progressSlider.setEnabled(false);
        controlsPane.add(progressSlider);

        // Volume Label
        JLabel volumeLabel = new JLabel("Volume");
        volumeLabel.setBounds(10, 70, 230, 15);
        controlsPane.add(volumeLabel);

        // Volume Slider
        JSlider volumeSlider = new JSlider(0, 20, 10);
        volumeSlider.setMajorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setBounds(10, 90, 230, 50);
        controlsPane.add(volumeSlider);

        // --- Music Section ---
        JPanel musicPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        musicPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Music",
                TitledBorder.LEFT, TitledBorder.TOP));
        musicPanel.setBounds(10, 150, 230, 100);
        musicPanel.add(new JButton("Play"));
        musicPanel.add(new JButton("Pause"));
        musicPanel.add(new JButton("Fade Out"));
        controlsPane.add(musicPanel);

        // --- SFX Section ---
        JPanel sfxPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        sfxPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "SFX",
                TitledBorder.LEFT, TitledBorder.TOP));
        sfxPanel.setBounds(10, 260, 230, 330);
        sfxPanel.add(new JButton("Door Open"));
        sfxPanel.add(new JButton("Win"));
        sfxPanel.add(new JButton("Arrow Flyby"));
        sfxPanel.add(new JButton("Magic Cast"));
        sfxPanel.add(new JButton("Potion Use"));
        sfxPanel.add(new JButton("Rolled 1"));
        sfxPanel.add(new JButton("Rolled 20"));
        controlsPane.add(sfxPanel);

        frame.setVisible(true);
    });
}
