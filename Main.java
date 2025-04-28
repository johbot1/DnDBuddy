import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.nio.file.Paths;

public class Main {
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
            scriptViewer.setMargin(new Insets(10, 10, 10, 10));
            frame.add(new JScrollPane(scriptViewer), BorderLayout.CENTER);

            // --- Controls Pane (East) ---
            JPanel controlsPane = new JPanel();
            controlsPane.setPreferredSize(new Dimension(250, 0));
            controlsPane.setLayout(null);  // Absolute positioning
            frame.add(controlsPane, BorderLayout.EAST);

            // Scene/Act Label
            JLabel sceneLabel = new JLabel("Loading…");
            sceneLabel.setFont(sceneLabel.getFont().deriveFont(Font.BOLD, 14f));
            sceneLabel.setBounds(10, 10, 230, 25);
            controlsPane.add(sceneLabel);

            // Progress Slider (music playback position)
            JSlider progressSlider = new JSlider(0, 100, 0);
            progressSlider.setBounds(10, 40, 230, 20);
            progressSlider.setEnabled(false);
            controlsPane.add(progressSlider);

            // Volume Label + Slider
            JLabel volumeLabel = new JLabel("Volume");
            volumeLabel.setBounds(10, 70, 230, 15);
            controlsPane.add(volumeLabel);

            JSlider volumeSlider = new JSlider(0, 20, 10);
            volumeSlider.setMajorTickSpacing(5);
            volumeSlider.setPaintTicks(true);
            volumeSlider.setPaintLabels(true);
            volumeSlider.setBounds(10, 90, 230, 50);
            controlsPane.add(volumeSlider);

            // Music panel (controls only; no dynamic content yet)
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

            // SFX panel (will be populated dynamically)
            JPanel sfxPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            sfxPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                    "SFX",
                    TitledBorder.LEFT, TitledBorder.TOP));
            sfxPanel.setBounds(10, 260, 230, 330);
            controlsPane.add(sfxPanel);

            // --- Load & parse the script ---
            try {
                // Adjust path to your script file
                var parsed = ScriptParser.parse(
                        Paths.get("scripts/act1_scene2.txt")
                );

                // 1) Update scene label
                sceneLabel.setText(
                        String.format("Act %d, Scene %d – %s",
                                parsed.getAct(),
                                parsed.getScene(),
                                parsed.getTitle()
                        )
                );

                // 2) Set background music file (store parsed.getMusicFile() for later)
                String bgm = parsed.getMusicFile();
                // TODO: load bgm into MediaPlayer

                // 3) Populate SFX panel with parsed cues
                sfxPanel.removeAll();
                for (String cue : parsed.getSfxCues()) {
                    JButton btn = new JButton(cue);
                    btn.addActionListener(e -> {
                        // TODO: play cue + ".mp3"
                        System.out.println("Play SFX: " + cue);
                    });
                    sfxPanel.add(btn);
                }
                controlsPane.revalidate();
                controlsPane.repaint();

                // 4) Fill script viewer with cleaned text
                scriptViewer.setText(parsed.getText());

                // 5) Collect clickable terms for later styling
                var terms = parsed.getClickableTerms();
                // TODO: walk the document, find these terms, and attach mouse listeners

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        frame,
                        "[MAIN - 5] Failed to load script: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            frame.setVisible(true);
        });
    }
}
