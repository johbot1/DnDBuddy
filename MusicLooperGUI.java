import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

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
    private JButton btnPlay, btnPause, btnStop, btnClear, btnClearAll;
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
        // We need to create the model here so we can pass it to the service
        fileListModel = new DefaultListModel<>();

        // Create the instance of our audio engine, passing it the new components.
        this.audioService = new AudioService(
                frmFoundation, // Pass the frame for centering dialogs
                fileListModel, // Pass the list model for the service to manage
                currentMicroseconds -> { // The time update callback
                    if (!boolIsUserDragging) {
                        sldrTimelineSlider.setValue((int) (currentMicroseconds / 1_000_000));
                        lblStartTime.setText(audioService.formatTime(currentMicroseconds));
                    }
                },
                this::getCurrentConfigFromUI, // The config provider
                () -> chkEnableLoop.isSelected(), // The loop enabled provider
                () -> chkEnableLoop.setSelected(false) // The loop finish callback
        );

        frmFoundation = new JFrame("Groove Buddy - Music Looper");
        frmFoundation.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frmFoundation.setLayout(new BorderLayout());

        JPanel mainControlPanel = new JPanel(new BorderLayout(Constants.GB_HGAP_SPACING, Constants.GB_HGAP_SPACING));
        mainControlPanel.add(createLoopControlsPanel(), BorderLayout.CENTER);
        mainControlPanel.add(createPlaybackControlsPanel(), BorderLayout.SOUTH);

        JPanel fileBrowserPanel = pnlCreateFileBrowser();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainControlPanel, fileBrowserPanel);
        splitPane.setResizeWeight(Constants.GB_SPLITPANE_RESIZE);

        frmFoundation.add(splitPane, BorderLayout.CENTER);

        setPlaybackButtonsEnabled(false);
        setLoopControlsEnabled(false);

        frmFoundation.setSize(Constants.GB_FOUNDATIONPANEL_WIDTH, Constants.GB_FOUNDATIONPANEL_HEIGHT);
        frmFoundation.setLocationRelativeTo(null);
        frmFoundation.setVisible(true);
        frmFoundation.setResizable(true);
    }

    // --- Creators ---
    /**
     * Creates the file browser panel. The open folder button now delegates to the service.
     */
    private JPanel pnlCreateFileBrowser() {
        JPanel pnlFileBrowser = new JPanel(new BorderLayout(Constants.GB_VGAP_SPACING, Constants.GB_VGAP_SPACING));
        pnlFileBrowser.setBorder(BorderFactory.createTitledBorder("Audio Files"));

        btnOpenFolder = new JButton("Open Folder");
        // Button now calls the service's method.
        btnOpenFolder.addActionListener(e -> audioService.openFolder());
        pnlFileBrowser.add(btnOpenFolder, BorderLayout.NORTH);

        // The fileListModel is now created in initUI and passed to the service.
        // The service is responsible for adding/removing files.
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
        pnlFileBrowser.add(scrollPane, BorderLayout.CENTER);
        pnlFileBrowser.setPreferredSize(Constants.GB_FILEBROWSER_DIMENSION);
        return pnlFileBrowser;
    }

    /**
     * Creates the panel containing the Play, Pause, and Stop buttons.
     *
     * @return A JPanel with the playback action buttons.
     */
    private JPanel createButtonPanel() {
        JPanel pnlButtonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, Constants.GB_HGAP_SPACING, Constants.GB_VGAP_SPACING));
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
        Dimension buttonSize = Constants.GB_BUTTON_SIZE;
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
        JPanel pnlTimeline = new JPanel(new BorderLayout(Constants.GB_HGAP_SPACING, 0));
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
     * Creates the Central Panel for loop settings
     *
     * @return A JPanel containing all loop control components
     */
    private JPanel createLoopControlsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Loop Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Constants.GB_INSET_VALS, Constants.GB_INSET_VALS, Constants.GB_INSET_VALS, Constants.GB_INSET_VALS);
        gbc.anchor = GridBagConstraints.WEST;

        SimpleDocumentListener listener = e -> { if (!updatingUI) audioService.updateCurrentConfig(getCurrentConfigFromUI()); };

        //TODO: Figure out a way to simplify these; Loop over them?

        // --- Row 0: Loop Start ---
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Loop Start:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; txtLoopStart = new JTextField("00:00.000", 8); txtLoopStart.getDocument().addDocumentListener(listener); panel.add(txtLoopStart, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; btnSetLoopStart = new JButton("Set"); btnSetLoopStart.addActionListener(e -> setLoopPoint(txtLoopStart)); panel.add(btnSetLoopStart, gbc);

        // --- Row 1: Loop End ---
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Loop End:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; txtLoopEnd = new JTextField("00:00.000", 8); txtLoopEnd.getDocument().addDocumentListener(listener); panel.add(txtLoopEnd, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; btnSetLoopEnd = new JButton("Set"); btnSetLoopEnd.addActionListener(e -> setLoopPoint(txtLoopEnd)); panel.add(btnSetLoopEnd, gbc);

        // --- Row 2: Repetitions & Clear Buttons (Modified) ---
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Repetitions:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; txtLoopCount = new JTextField("1", 3); txtLoopCount.getDocument().addDocumentListener(listener); panel.add(txtLoopCount, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; chkInfiniteLoop = new JCheckBox("Infinite");
        chkInfiniteLoop.addActionListener(e -> {
            txtLoopCount.setEnabled(!chkInfiniteLoop.isSelected());
            if (!updatingUI) audioService.updateCurrentConfig(getCurrentConfigFromUI());
        });
        panel.add(chkInfiniteLoop, gbc);

        // --- Panel for the clear buttons with added functionality ---
        JPanel clearButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnClear = new JButton("Clear Current");
        btnClear.setToolTipText("Clear cues for the current track");
        btnClear.addActionListener(e -> {
            audioService.clearCurrentLoopConfig();
            refreshLoopUI();
        });

        btnClearAll = new JButton("Clear All");
        btnClearAll.setToolTipText("Clear all saved cues in this folder");
        btnClearAll.addActionListener(e -> {
            audioService.clearAllLoopConfigs();
            refreshLoopUI();
        });

        clearButtonsPanel.add(btnClear);
        clearButtonsPanel.add(btnClearAll);
        gbc.gridx = 3; // Place this panel in the last column
        panel.add(clearButtonsPanel, gbc);

        // --- Row 3: Enable Loop Checkbox ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; chkEnableLoop = new JCheckBox("Enable Loop"); panel.add(chkEnableLoop, gbc);

        // --- Row 4: Status Label ---
        gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; lblStatusLabel = new JLabel("Open a folder to begin.", SwingConstants.CENTER); panel.add(lblStatusLabel, gbc);

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
        pnlControlContainer.setBorder(Constants.GB_CONTROLCONTAINER_BORDER);
        pnlControlContainer.add(createTimelinePanel(), BorderLayout.NORTH);
        pnlControlContainer.add(createButtonPanel(), BorderLayout.CENTER);
        return pnlControlContainer;
    }


    // --- Setters ---
    /**
     * Sets the text of a target field to the current time on the timeline
     */
    private void setLoopPoint(JTextField targetField) {
        long currentTime = audioService.getCurrentMicroseconds();
        targetField.setText(audioService.formatTime(currentTime));
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

    /**
     * Resets the loop UI fields to their default state.
     */
    private void refreshLoopUI() {
        // Create a default config to get the initial values
        LoopConfig defaultConfig = new LoopConfig();

        // updatingUI flag to prevent listeners from saving these default values
        updatingUI = true;
        txtLoopStart.setText(defaultConfig.loopStart);
        txtLoopEnd.setText(defaultConfig.loopEnd);
        txtLoopCount.setText(String.valueOf(defaultConfig.repeats));
        chkInfiniteLoop.setSelected(defaultConfig.isInfinite);
        txtLoopCount.setEnabled(true); // Ensure the count is re-enabled
        updatingUI = false;
    }

    // --- Getters ---
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
        txtLoopCount.setEnabled(!details.config().isInfinite);
        chkInfiniteLoop.setSelected(details.config().isInfinite);
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
        config.isInfinite = chkInfiniteLoop.isSelected(); // Save the state of the new checkbox
        try {
            config.repeats = Integer.parseInt(txtLoopCount.getText());
        } catch (NumberFormatException e) {
            config.repeats = 1;
        }
        return config;
    }

}
