import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Set;

/**
 * Main application class for DM Buddy.
 * <p>
 * Manages the script viewer, scene controls, and media playback UI.
 */
public class Main {
    private JFrame frame;
    private LinkTextPane scriptViewer;
    private JLabel sceneLabel;
    private JPanel sfxPanel;
    private JPanel musicPanel;
    private JSlider volumeSlider;

    /**
     * Entry point; initializes the Swing UI.
     *
     * @param args command-line arguments (unused)
     * @throws IOException if UI initialization or script loading fails
     */
    public static void main(String[] args) throws IOException {
        new Main().initUI();
    }

    /**
     * Sets up the main application window, layout, and loads the initial script.
     *
     * @throws IOException if the script file cannot be read
     */
    private void initUI() throws IOException {
        frame = new JFrame("DM Buddy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Script viewer pane
        scriptViewer = createScriptViewer();
        frame.add(new JScrollPane(scriptViewer), BorderLayout.CENTER);

        // Controls side panel
        frame.add(createControlsPane(), BorderLayout.EAST);

        // Load default script
        Path scriptPath = Paths.get("resources/scripts/act1_scene1.txt");
        validateScriptPath(scriptPath);
        loadScript(scriptPath);

        frame.setVisible(true);
    }

    /**
     * Verifies the existence of the script file and exits on failure.
     *
     * @param path the path to the script file
     */
    private void validateScriptPath(Path path) {
        if (!Files.exists(path)) {
            JOptionPane.showMessageDialog(frame,
                    "Script not found: " + path,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Creates and configures the non-editable text pane for displaying scripts.
     *
     * @return a configured LinkTextPane instance
     */
    private LinkTextPane createScriptViewer() {
        LinkTextPane pane = new LinkTextPane();
        pane.setEditable(false);
        pane.setMargin(new Insets(10, 10, 10, 10));
        return pane;
    }

    /**
     * Builds the side panel containing scene info, volume slider,
     * music buttons, and placeholders for SFX buttons.
     *
     * @return a JPanel with all controls assembled
     */
    private JPanel createControlsPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Scene label
        sceneLabel = new JLabel("Loading...");
        sceneLabel.setFont(sceneLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(sceneLabel);
        panel.add(Box.createVerticalStrut(10));

        // Volume control
        panel.add(new JLabel("Volume"));
        volumeSlider = new JSlider(0, 20, 10);
        volumeSlider.setMajorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        panel.add(volumeSlider);
        panel.add(Box.createVerticalStrut(10));

        // Music controls
        musicPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        musicPanel.setBorder(BorderFactory.createTitledBorder("Music"));
        musicPanel.add(createButton("Play", e -> playMusic()));
        musicPanel.add(createButton("Pause", e -> pauseMusic()));
        musicPanel.add(createButton("Fade Out", e -> fadeOutMusic()));
        panel.add(musicPanel);
        panel.add(Box.createVerticalStrut(10));

        // SFX placeholder panel
        sfxPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        sfxPanel.setBorder(BorderFactory.createTitledBorder("SFX"));
        panel.add(sfxPanel);

        return panel;
    }

    /**
     * Helper to create a JButton with provided label and action listener.
     *
     * @param text     button label
     * @param listener action to perform on click
     * @return a configured JButton
     */
    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.addActionListener(listener);
        return btn;
    }

    /**
     * Parses the script file, updates UI components, and populates SFX buttons.
     *
     * @param scriptPath path to the script file
     * @throws IOException if parsing or file reading fails
     */
    private void loadScript(Path scriptPath) throws IOException {
        var parsed = ScriptParser.parse(scriptPath);

        // Update scene heading
        sceneLabel.setText(String.format("Act %d, Scene %d – %s",
                parsed.getAct(), parsed.getScene(), parsed.getTitle()));

        // Load background music if available
        if (parsed.getMusicFile() != null) {
            loadMusic(parsed.getMusicFile());
        }

        // Rebuild SFX buttons
        sfxPanel.removeAll();
        for (String cue : parsed.getSfxCues()) {
            sfxPanel.add(createButton(cue, e -> playSfx(cue)));
        }
        sfxPanel.revalidate();

        // Display script text with clickable terms
        scriptViewer.setText(parsed.getText());
        Set<String> terms = parsed.getClickableTerms();
        scriptViewer.setClickableTerms(terms);
        styleClickableTerms(scriptViewer, terms);
        styleMusicCues(scriptViewer,parsed.getMusicCues());
    }

    /**
     * Verifies the presence of a character sheet file.
     *
     * @param path path to the sheet file
     */
    private void validateSheetPath(Path path) {
        if (!Files.exists(path)) {
            System.err.println("Sheet not found: " + path);
        }
    }

    /**
     * Styles specified terms as clickable links and registers click handler
     * to display character sheets.
     *
     * @param pane  text pane containing script
     * @param terms set of terms to highlight
     */
    private void styleClickableTerms(JTextPane pane, Set<String> terms) {
        StyledDocument doc = pane.getStyledDocument();
        Style linkStyle = doc.addStyle("link", null);
        StyleConstants.setForeground(linkStyle, Color.BLUE);
        StyleConstants.setUnderline(linkStyle, true);

        String text = pane.getText();
        for (String term : terms) {
            int idx = 0;
            while ((idx = text.indexOf(term, idx)) >= 0) {
                doc.setCharacterAttributes(idx, term.length(), linkStyle, false);
                idx += term.length();
            }
        }

        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    int pos = pane.viewToModel2D(e.getPoint());
                    int start = Utilities.getWordStart(pane, pos);
                    int end = Utilities.getWordEnd(pane, pos);
                    String fullText = pane.getText();
                    String clicked = fullText.substring(start, end);
                    if (terms.contains(clicked)) showCharacterSheet(clicked);
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Reads and displays the character sheet in a dialog.
     *
     * @param name character name matching the sheet file
     */
    private void showCharacterSheet(String name) {
        Path sheetPath = Paths.get("resources/sheets", name + ".txt");
        validateSheetPath(sheetPath);
        String content;
        try {
            content = Files.readString(sheetPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JOptionPane.showMessageDialog(frame, content, name, JOptionPane.PLAIN_MESSAGE);
    }


    private void styleMusicCues(LinkTextPane pane, Set<String> cues) throws BadLocationException {
        StyledDocument doc = pane.getStyledDocument();
        Style musicStyle = doc.addStyle("music", null);
        StyleConstants.setForeground(musicStyle, Color.RED);

        String text = doc.getText(0, doc.getLength());
        for (String cue : cues) {
            int idx = text.indexOf("[" + cue + "]");
            while (idx >= 0) {
                // Apply style only to inside of brackets
                doc.setCharacterAttributes(idx, cue.length() + 2, musicStyle, false);
                idx = text.indexOf("[" + cue + "]", idx) + 1;
            }
        }
    }
    // Media control stubs – replace with real audio handling as needed
    /** Stub: load music file for playback. */
    private void loadMusic(String file) { System.out.println("Load music: " + file); }
    /** Stub: start music playback. */
    private void playMusic() { System.out.println("Play music"); }
    /** Stub: pause music playback. */
    private void pauseMusic() { System.out.println("Pause music"); }
    /** Stub: fade out current music. */
    private void fadeOutMusic() { System.out.println("Fade out music"); }
    /** Stub: play a sound effect cue. */
    private void playSfx(String cue) { System.out.println("Play SFX: " + cue); }
}
