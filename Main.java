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
import java.util.Set;

class Main extends JFrame {
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

        // --- Load, parse, and bind the script ---
        bindScriptToUI(
                scriptViewer,        // your text pane
                (JPanel) controlsPane, // cast if needed to access sub‐components
                Paths.get("resources/scripts/act1_scene1.txt"),
                frame
        );

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

private static void bindScriptToUI(JTextPane scriptViewer, JPanel controlsPane, Path scriptPath, JFrame frame) {
    try {
        var parsed = ScriptParser.parse(scriptPath);

        // 1) Scene Label
        JLabel sceneLabel = (JLabel) findComponent(controlsPane, JLabel.class, 0);
        sceneLabel.setText(
                String.format("Act %d, Scene %d – %s",
                        parsed.getAct(),
                        parsed.getScene(),
                        parsed.getTitle()
                )
        );

        // 2) Populate SFX buttons
        JPanel sfxPanel = (JPanel) findComponent(controlsPane, JPanel.class, 1);
        sfxPanel.removeAll();
        for (String cue : parsed.getSfxCues()) {
            JButton btn = new JButton(cue);
            btn.addActionListener(e -> System.out.println("Play SFX: "+cue));
            sfxPanel.add(btn);
        }
        controlsPane.revalidate();
        controlsPane.repaint();

        // 3) Script Text
        String text = parsed.getText();
        scriptViewer.setText(text);

        // 4) Style + listeners for clickable terms
        styleClickableTerms(scriptViewer, parsed.getClickableTerms(), frame);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
                frame,
                "Failed to load script: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

// Utility to find a child component of a given type/index in a panel
private static JComponent findComponent(Container parent, Class<? extends JComponent> cls, int index) {
    int count = 0;
    for (Component c : parent.getComponents()) {
        if (cls.isInstance(c)) {
            if (count++ == index) return (JComponent)c;
        }
    }
    throw new IllegalStateException("No component found");
}

// All your previous styling/MouseListener code goes here:
private static void styleClickableTerms(JTextPane scriptViewer, Set<String> terms, JFrame frame) {
    StyledDocument doc = scriptViewer.getStyledDocument();
    Style linkStyle = doc.addStyle("link", null);
    StyleConstants.setForeground(linkStyle, Color.BLUE);
    StyleConstants.setUnderline(linkStyle, true);

    String fullText = scriptViewer.getText();
    for (String term : terms) {
        int idx = 0;
        while ((idx = fullText.indexOf(term, idx)) >= 0) {
            doc.setCharacterAttributes(idx, term.length(), linkStyle, false);
            idx += term.length();
        }
    }

    scriptViewer.addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            try {
                int pos = scriptViewer.viewToModel2D(e.getPoint());
                int st  = Utilities.getWordStart(scriptViewer, pos);
                int en  = Utilities.getWordEnd  (scriptViewer, pos);
                String clicked = doc.getText(st, en-st);
                if (terms.contains(clicked)) {
                    String content = Files.readString(
                            Paths.get("resources/sheets", clicked+".txt")
                    );
                    JOptionPane.showMessageDialog(frame, content, clicked,
                            JOptionPane.PLAIN_MESSAGE);
                }
            } catch (Exception ignored) {}
        }
    });
    scriptViewer.addMouseMotionListener(new MouseMotionAdapter() {
        @Override public void mouseMoved(MouseEvent e) {
            try {
                int pos = scriptViewer.viewToModel2D(e.getPoint());
                int st  = Utilities.getWordStart(scriptViewer, pos);
                int en  = Utilities.getWordEnd  (scriptViewer, pos);
                String over = doc.getText(st, en-st);
                if (terms.contains(over)) {
                    scriptViewer.setCursor(Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR));
                    scriptViewer.setToolTipText("Open sheet for \""+over+"\"");
                } else {
                    scriptViewer.setCursor(Cursor.getDefaultCursor());
                    scriptViewer.setToolTipText(null);
                }
            } catch (BadLocationException ignored) {}
        }
    });
}
}