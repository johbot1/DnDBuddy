import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.function.Supplier;

/**
 * MusicLooperGUI sets up the main graphical user interface for the music looper application.
 * It includes playback controls, a timeline slider, and a button to load audio files.
 * This initial version focuses only on the layout and component placement.
 */
public class MusicLooperGUI {

    // --- UI Components ---
    private JFrame frmFoundation;
    private JLabel lblStatusLabel;
    private JLabel lblStartTime;
    private JLabel lblEndTime;
    private JSlider sldrTimelineSlider;
    private JButton btnPlay, btnPause, btnStop;
    private JTextField txtLoopStart, txtLoopEnd, txtLoopCount;
    private JButton btnSetLoopStart, btnSetLoopEnd;
    private JCheckBox chkEnableLoop, chkInfiniteLoop;
    private JList<File> fileList;
    private DefaultListModel<File> fileListModel;
    private JButton btnOpenFolder;

    // --- Backend Service ---
    private AudioService audioService;

    // --- State Flags ---
    private boolean boolIsUserDragging = false;
    private boolean updatingUI = false;

    /**
     * Initializes the main frame and all its UI components.
     */
    public void initUI() {
        // This is the function that provides the current loop settings from the UI fields.
        Supplier<LoopConfig> loopConfigProvider = this::getCurrentConfigFromUI;

        // Create the instance of our audio engine, passing it all the necessary functions.
        this.audioService = new AudioService(
                // 1. Update the time display
                currentMicroseconds -> {
                    if (!boolIsUserDragging) {
                        long currentSeconds = currentMicroseconds / 1_000_000;
                        sldrTimelineSlider.setValue((int) currentSeconds);
                        lblStartTime.setText(audioService.formatTime(currentMicroseconds));
                    }
                },
                // 2. Get the current loop settings
                loopConfigProvider,
                // 3. Check if the loop is enabled
                () -> chkEnableLoop.isSelected(),
                // 4. Run when looping finishes
                () -> chkEnableLoop.setSelected(false)
        );

        // --- UI Setup ---
        frmFoundation = new JFrame("Groove Buddy - Music Looper");
        frmFoundation.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frmFoundation.setLayout(new BorderLayout());

        JPanel mainControlPanel = new JPanel(new BorderLayout(10, 10));
        mainControlPanel.add(createLoopControlsPanel(), BorderLayout.CENTER);
        mainControlPanel.add(createPlaybackControlsPanel(), BorderLayout.SOUTH);

        JPanel fileBrowserPanel = pnlCreateFileBrowser();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainControlPanel, fileBrowserPanel);
        splitPane.setResizeWeight(0.8);

        frmFoundation.add(splitPane, BorderLayout.CENTER);

        setPlaybackButtonsEnabled(false);
        setLoopControlsEnabled(false);

        frmFoundation.setSize(1000, 600);
        frmFoundation.setLocationRelativeTo(null);
        frmFoundation.setVisible(true);
        frmFoundation.setResizable(true);
    }

    /**
     * Creates the file browser panel on the RIGHT side of the UI
     */
    private JPanel pnlCreateFileBrowser() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Audio Files"));

        btnOpenFolder = new JButton("Open Folder");
        btnOpenFolder.addActionListener(e -> openFolder());
        panel.add(btnOpenFolder, BorderLayout.NORTH);

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setCellRenderer(new FileNameRenderer());

        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                File selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    AudioService.AudioDetails details = audioService.loadFile(selectedFile);
                    if (details != null) {
                        updateUIWithAudioDetails(selectedFile.getName(), details);
                    } else {
                        JOptionPane.showMessageDialog(frmFoundation, "Could not load the selected audio file.", "Audio Load Error", JOptionPane.ERROR_MESSAGE);
                        setPlaybackButtonsEnabled(false);
                        setLoopControlsEnabled(false);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileList);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(200, 0));
        return panel;
    }

    /**
     * Updates all UI Components based on a loaded file
     *
     * @param fileName The name of the selected audio file
     * @param details  Details such as ms length and configuration
     */
    private void updateUIWithAudioDetails(String fileName, AudioService.AudioDetails details) {
        long durationSeconds = details.durationMicroseconds() / 1_000_000;
        sldrTimelineSlider.setMaximum((int) durationSeconds);
        lblEndTime.setText(audioService.formatTime(details.durationMicroseconds()));
        lblStatusLabel.setText("Loaded: " + fileName);

        updatingUI = true;
        txtLoopStart.setText(details.config().loopStart);
        txtLoopEnd.setText(details.config().loopEnd);
        txtLoopCount.setEnabled(!details.config().boolInfinite);
        chkInfiniteLoop.setSelected(details.config().boolInfinite);
        updatingUI = false;

        audioService.stop(); // Reset player to a clean state
        btnPlay.setText("Play");
        setPlaybackButtonsEnabled(true);
        setLoopControlsEnabled(true);
    }

    /**
     * Gets the current loop settings from the UI fields into a LoopConfig
     *
     * @return Returns a configuration defined by UI elements
     */
    private LoopConfig getCurrentConfigFromUI() {
        LoopConfig config = new LoopConfig();
        config.loopStart = txtLoopStart.getText();
        config.loopEnd = txtLoopEnd.getText();
        config.boolInfinite = chkInfiniteLoop.isSelected(); // Save the state of the new checkbox
        try {
            config.repeats = Integer.parseInt(txtLoopCount.getText());
        } catch (NumberFormatException e) {
            config.repeats = 1;
        }
        return config;
    }

    /**
     * Sets the text of a target field to the current time on the timeline
     */
    private void setLoopPoint(JTextField targetField) {
        long currentTime = audioService.getCurrentMicroseconds();
        targetField.setText(audioService.formatTime(currentTime));
    }

    /**
     * Creates the panel containing the Play, Pause, and Stop buttons.
     *
     * @return A JPanel with the playback action buttons.
     */
    private JPanel createButtonPanel() {
        JPanel pnlButtonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPlay = new JButton("Play");
        btnPlay.addActionListener(e -> {
            audioService.play();
            btnPlay.setText("Resume");
        });
        btnPause = new JButton("Pause");
        btnPause.addActionListener(e -> audioService.pause());
        btnStop = new JButton("Stop");
        btnStop.addActionListener(e -> {
            audioService.stop();
            btnPlay.setText("Play");
        });
        Dimension buttonSize = new Dimension(80, 30);
        btnPlay.setPreferredSize(buttonSize);
        btnPause.setPreferredSize(buttonSize);
        btnStop.setPreferredSize(buttonSize);
        pnlButtonContainer.add(btnPlay);
        pnlButtonContainer.add(btnPause);
        pnlButtonContainer.add(btnStop);
        return pnlButtonContainer;
    }

    /**
     * Creates the panel for the audio timeline, including the slider and time labels.
     *
     * @return A JPanel with the JSlider and time labels.
     */
    private JPanel createTimelinePanel() {
        JPanel pnlTimeline = new JPanel(new BorderLayout(10, 0));
        lblStartTime = new JLabel("00:00.000");
        lblEndTime = new JLabel("00:00.000");
        sldrTimelineSlider = new JSlider(0, 0, 0);
        sldrTimelineSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                boolIsUserDragging = true;
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                audioService.seek(sldrTimelineSlider.getValue() * 1_000_000L);
                lblStartTime.setText(audioService.formatTime(audioService.getCurrentMicroseconds()));
                boolIsUserDragging = false;
            }
        });
        sldrTimelineSlider.addChangeListener(e -> {
            if (boolIsUserDragging) {
                lblStartTime.setText(audioService.formatTime(sldrTimelineSlider.getValue() * 1_000_000L));
            }
        });
        pnlTimeline.add(lblStartTime, BorderLayout.WEST);
        pnlTimeline.add(sldrTimelineSlider, BorderLayout.CENTER);
        pnlTimeline.add(lblEndTime, BorderLayout.EAST);
        return pnlTimeline;
    }

    /**
     * Opens a JFileChooser to select a directory, then populates the file list
     * with supported audio files found inside
     */
    private void openFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setDialogTitle("Select Audio Folder");
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setAcceptAllFileFilterUsed(false);

        if (folderChooser.showOpenDialog(frmFoundation) == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            fileListModel.clear();
            File[] audioFiles = selectedFolder.listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".wav") || lowerName.endsWith(".au") || lowerName.endsWith(".mp3");
            });
            if (audioFiles != null) {
                for (File file : audioFiles) {
                    fileListModel.addElement(file);
                }
            }
        }
    }

    /**
     * Creates the Central Panel for loop settings
     *
     * @return A JPanel containing all loop control components
     */
    private JPanel createLoopControlsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Loop Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        SimpleDocumentListener listener = e -> {
            if (!updatingUI) {
                audioService.updateCurrentConfig(getCurrentConfigFromUI());
            }
        };

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Loop Start:"), gbc);
        gbc.gridx = 1;
        txtLoopStart = new JTextField("00:00.000", 8);
        txtLoopStart.getDocument().addDocumentListener(listener);
        panel.add(txtLoopStart, gbc);
        gbc.gridx = 2;
        btnSetLoopStart = new JButton("Set");
        btnSetLoopStart.addActionListener(e -> setLoopPoint(txtLoopStart));
        panel.add(btnSetLoopStart, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Loop End:"), gbc);
        gbc.gridx = 1;
        txtLoopEnd = new JTextField("00:00.000", 8);
        txtLoopEnd.getDocument().addDocumentListener(listener);
        panel.add(txtLoopEnd, gbc);
        gbc.gridx = 2;
        btnSetLoopEnd = new JButton("Set");
        btnSetLoopEnd.addActionListener(e -> setLoopPoint(txtLoopEnd));
        panel.add(btnSetLoopEnd, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Repetitions:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        txtLoopCount = new JTextField("1", 3);
        txtLoopCount.getDocument().addDocumentListener(listener);
        panel.add(txtLoopCount, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        chkInfiniteLoop = new JCheckBox("Infinite");
        chkInfiniteLoop.addActionListener(e -> {
            txtLoopCount.setEnabled(!chkInfiniteLoop.isSelected());
            if (!updatingUI) audioService.updateCurrentConfig(getCurrentConfigFromUI());
        });
        panel.add(chkInfiniteLoop, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        chkEnableLoop = new JCheckBox("Enable Loop");
        panel.add(chkEnableLoop, gbc);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lblStatusLabel = new JLabel("Open a folder to begin.", SwingConstants.CENTER);
        panel.add(lblStatusLabel, gbc);

        return panel;
    }

    /**
     * Creates the main panel for all playback controls, including the timeline and buttons.
     * This panel will be placed at the bottom of the window.
     *
     * @return A JPanel containing the timeline and playback buttons.
     */
    private JPanel createPlaybackControlsPanel() {
        JPanel pnlControlContainer = new JPanel(new BorderLayout());
        pnlControlContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlControlContainer.add(createTimelinePanel(), BorderLayout.NORTH);
        pnlControlContainer.add(createButtonPanel(), BorderLayout.CENTER);
        return pnlControlContainer;
    }

    /**
     * Enables or disables the playback control buttons
     *
     * @param enabled true to enable, false to disable
     */
    private void setPlaybackButtonsEnabled(boolean enabled) {
        btnPlay.setEnabled(enabled);
        btnPause.setEnabled(enabled);
        btnStop.setEnabled(enabled);
    }

    /**
     * Enables or disables the loop control buttons
     *
     * @param enabled true to enable, false to disable
     */
    private void setLoopControlsEnabled(boolean enabled) {
        txtLoopStart.setEnabled(enabled);
        txtLoopEnd.setEnabled(enabled);
        txtLoopCount.setEnabled(enabled);
        btnSetLoopStart.setEnabled(enabled);
        btnSetLoopEnd.setEnabled(enabled);
        chkEnableLoop.setEnabled(enabled);
        chkInfiniteLoop.setEnabled(enabled);
    }
}
