import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        // --- Frame Setup (Phase 1) ---
        JFrame frame = new JFrame("DM Buddy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // --- Script Viewer (Center) ---
        JTextPane scriptViewer = createScriptViewer();
        frame.add(new JScrollPane(scriptViewer), BorderLayout.CENTER);

        // --- Controls Pane (East) ---
        JPanel controlsPane = createControlsPane();
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
            var parsed = ScriptParser.parse(
                    Paths.get("resources/scripts/act1_scene1.txt")
            );

            // ─── INSERT PARSER SMOKE-TEST HERE ───
            System.out.println("ACT:    " + parsed.getAct());
            System.out.println("SCENE:  " + parsed.getScene());
            System.out.println("TITLE:  " + parsed.getTitle());
            System.out.println("MUSIC:  " + parsed.getMusicFile());
            System.out.println("SFX:    " + parsed.getSfxCues());
            System.out.println("TERMS:  " + parsed.getClickableTerms());
            System.out.println("TEXT:\n" + parsed.getText());
            // ─────────────────────────────────────

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
            String text = parsed.getText();
            scriptViewer.setText(text);



            // 5) Collect clickable terms for later styling
            var terms = parsed.getClickableTerms();
            // ─── START CLICKABLE-TERM STYLING & HANDLERS ───
            // 1) Prepare a blue-underlined “link” style
            StyledDocument doc = scriptViewer.getStyledDocument();
            Style linkStyle = doc.addStyle("link", null);
            StyleConstants.setForeground(linkStyle, Color.BLUE);
            StyleConstants.setUnderline(linkStyle, true);

            // 2) Apply it to each occurrence of every term
            String fullText = text;
            for (String term : terms) {
                int idx = 0;
                while ((idx = fullText.indexOf(term, idx)) >= 0) {
                    doc.setCharacterAttributes(idx, term.length(), linkStyle, false);
                    idx += term.length();
                }
            }

            // 3) On click, open the corresponding sheet file
            scriptViewer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        int pos   = scriptViewer.viewToModel2D(e.getPoint());
                        int start = Utilities.getWordStart(scriptViewer, pos);
                        int end   = Utilities.getWordEnd  (scriptViewer, pos);
                        String clicked = doc.getText(start, end - start);
                        if (terms.contains(clicked)) {
                            Path sheet = Paths.get("resources/sheets", clicked + ".txt");
                            String content = Files.readString(sheet);
                            JOptionPane.showMessageDialog(
                                    frame, content, clicked, JOptionPane.PLAIN_MESSAGE
                            );
                        }
                    } catch (Exception ignored) {}
                }
            });

            // 4) On hover, show hand cursor + tooltip
            scriptViewer.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    try {
                        int pos   = scriptViewer.viewToModel2D(e.getPoint());
                        int start = Utilities.getWordStart(scriptViewer, pos);
                        int end   = Utilities.getWordEnd  (scriptViewer, pos);
                        String over = doc.getText(start, end - start);
                        if (terms.contains(over)) {
                            scriptViewer.setCursor(Cursor.getPredefinedCursor(
                                    Cursor.HAND_CURSOR));
                            scriptViewer.setToolTipText("Open sheet for \"" + over + "\"");
                        } else {
                            scriptViewer.setCursor(Cursor.getDefaultCursor());
                            scriptViewer.setToolTipText(null);
                        }
                    } catch (BadLocationException ignored) {}
                }
            });
            // ─── END CLICKABLE-TERM STYLING & HANDLERS ───


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
// Creates and returns the configured JTextPane for your script
private static JTextPane createScriptViewer() {
    JTextPane tp = new JTextPane();
    tp.setEditable(false);
    tp.setMargin(new Insets(10, 10, 10, 10));
    return tp;
}

// Creates and returns the right-side panel (scene label, sliders, Music/SFX groups)
private static JPanel createControlsPane() {
    JPanel panel = new JPanel();
    panel.setPreferredSize(new Dimension(250, 0));
    panel.setLayout(null);

    // Scene/Act Label
    JLabel sceneLabel = new JLabel("Loading…");
    sceneLabel.setFont(sceneLabel.getFont().deriveFont(Font.BOLD, 14f));
    sceneLabel.setBounds(10, 10, 230, 25);
    panel.add(sceneLabel);

    // Progress Slider
    JSlider progressSlider = new JSlider(0, 100, 0);
    progressSlider.setBounds(10, 40, 230, 20);
    progressSlider.setEnabled(false);
    panel.add(progressSlider);

    // Volume
    JLabel volumeLabel = new JLabel("Volume");
    volumeLabel.setBounds(10, 70, 230, 15);
    panel.add(volumeLabel);

    JSlider volumeSlider = new JSlider(0, 20, 10);
    volumeSlider.setMajorTickSpacing(5);
    volumeSlider.setPaintTicks(true);
    volumeSlider.setPaintLabels(true);
    volumeSlider.setBounds(10, 90, 230, 50);
    panel.add(volumeSlider);

    // Music Section
    JPanel musicPanel = new JPanel(new GridLayout(0, 1, 5, 5));
    musicPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
            "Music", TitledBorder.LEFT, TitledBorder.TOP));
    musicPanel.setBounds(10, 150, 230, 100);
    musicPanel.add(new JButton("Play"));
    musicPanel.add(new JButton("Pause"));
    musicPanel.add(new JButton("Fade Out"));
    panel.add(musicPanel);

    // SFX Section (empty for now)
    JPanel sfxPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    sfxPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
            "SFX", TitledBorder.LEFT, TitledBorder.TOP));
    sfxPanel.setBounds(10, 260, 230, 330);
    panel.add(sfxPanel);

    return panel;
}