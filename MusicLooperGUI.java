import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MusicLooperGUI sets up the main graphical user interface for the music looper application.
 * It includes playback controls, a timeline slider, and a button to load audio files.
 * This initial version focuses only on the layout and component placement.
 */
public class MusicLooperGUI {

    // A logger for logging messages for this class
    private static final Logger LOGGER = Logger.getLogger(MusicLooperGUI.class.getName());

    // -- GUI Components --
    private JFrame frmFoundation;
    private JLabel lblStatusLabel;
    private JLabel lblStartTime;
    private JLabel lblEndTime;
    private JSlider sldrTimelineSlider;
    private JButton btnPlay, btnPause, btnStop;

    // -- Loop Control Components --
    private JTextField txtLoopStart, txtLoopEnd, txtLoopCount;
    private JButton btnSetLoopStart, btnSetLoopEnd;
    private JCheckBox chkEnableLoop;


    //A storage area for loaded audio data
    private Clip clpAudioClip;
    // Timer for updating timeline slider
    private Timer tmrTimeline;
    //Counter for repeats
    private int intRepeatsRemaining;
    private boolean boolIsUserDragging;

    // -- File Browser Components --
    private JList<File> fileList;
    private DefaultListModel<File> fileListModel;
    private JButton btnOpenFolder;

    /**
     * The main entry point for the application.
     * It schedules the GUI creation on the Event Dispatch Thread.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Swing applications should be run on the Event Dispatch Thread (EDT)
        // to ensure thread safety for GUI components.
        SwingUtilities.invokeLater(() -> {
            try {
                // Use the system's look and feel for a native appearance.
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new MusicLooperGUI().initUI();
            } catch (Exception e) {
                // Log the exception with a severe level and show a user-friendly error dialog.
                LOGGER.log(Level.SEVERE, "An unexpected error occurred during GUI initialization.", e);
                JOptionPane.showMessageDialog(null,
                        "A critical error occurred and the application cannot start.\n" +
                                "Please check the logs for more details.",
                        "Application Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Initializes the main frame and all its UI components.
     */
    private void initUI() {
        // Create the main window (JFrame)
        frmFoundation = new JFrame("Groove Buddy - Music Looper");
        frmFoundation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmFoundation.setLayout(new BorderLayout(10, 10));

        // Add all the panels to the frame
        JPanel mainControlPanel = new JPanel(new BorderLayout(10,10));
        mainControlPanel.add(createLoopControlsPanel(),BorderLayout.CENTER);
        mainControlPanel.add(createPlaybackControlsPanel(), BorderLayout.SOUTH);

        JPanel fileBrowserPanel = pnlCreateFileBrowser();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainControlPanel, fileBrowserPanel);
        splitPane.setResizeWeight(0.8);

        frmFoundation.add(splitPane,BorderLayout.CENTER);


        lblStatusLabel = new JLabel("Load an audio file to begin");
        lblStatusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initial state for controls
        setPlaybackButtonsEnabled(false);
        setLoopControlsEnabled(false);
        // Initialize the Timer
        setupTimer();

        // Size the window and make it visible
        frmFoundation.pack(); // Sizes the window to fit the preferred size of its subcomponents
        frmFoundation.setSize(1000,600);//
//        frmFoundation.setMinimumSize(frmFoundation.getSize()); // Prevent resizing smaller than packed size
        frmFoundation.setLocationRelativeTo(null); // Center the window on the screen
        frmFoundation.setVisible(true);
    }

//    /**
//     * Creates the top panel which contains the file loading controls.
//     *
//     * @return A JPanel containing the load/unload button.
//     */
//    private JPanel createTopPanel() {
//        JPanel pnlTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        pnlTopPanel.setBorder(new EmptyBorder(5, 5, 0, 5)); // Add some padding
//
//        btnLoad = new JButton("Load Audio File");
//        btnLoad.addActionListener(e -> loadAudioFile());
//        pnlTopPanel.add(btnLoad);
//
//        return pnlTopPanel;
//    }

    /**
     * Creates the file browser panel on the RIGHT side of the UI
     * @return A JPanel containing the open folder button and a list of audio files
     */
    private JPanel pnlCreateFileBrowser(){
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.setBorder(BorderFactory.createTitledBorder("Audio Files"));

        //Button to open a folder
        btnOpenFolder = new JButton("Open Folder");
        btnOpenFolder.addActionListener(e -> openFolder());
        panel.add(btnOpenFolder,BorderLayout.NORTH);

        //List to display the files
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setCellRenderer(new FlieNameRenderer());

        //Listener to load the file when an item is selected
        fileList.addListSelectionListener(e-> {
            //Prevents (hopefully) the event firing twice
            if (!e.getValueIsAdjusting()){
                File selectedFile = fileList.getSelectedValue();
                if (selectedFile != null){
                    loadAudioFile(selectedFile);
                }
            }
        });

        //Put the list in a scrollable pane
        JScrollPane scrollPane = new JScrollPane(fileList);
        panel.add(scrollPane, BorderLayout.CENTER);

        //Preferred size start out
        panel.setPreferredSize(new Dimension(200,0));

        return panel;
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

        // -- Row 0: Loop Start --
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Loop Start: "), gbc);

        gbc.gridx = 1;
        txtLoopStart = new JTextField("00:00", 5);
        panel.add(txtLoopStart, gbc);

        gbc.gridx = 2;
        btnSetLoopStart = new JButton("Set");
        panel.add(btnSetLoopStart, gbc);

        // -- Row 1: Loop End --
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Loop End:"), gbc);

        gbc.gridx = 1;
        txtLoopEnd = new JTextField("00:00", 5);
        panel.add(txtLoopEnd, gbc);

        gbc.gridx = 2;
        btnSetLoopEnd = new JButton("Set");
        panel.add(btnSetLoopEnd, gbc);

        // -- Row 2: Repetitions --
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Repetitions:"), gbc);

        gbc.gridx = 1;
        txtLoopCount = new JTextField("1", 3);
        panel.add(txtLoopCount, gbc);

        // -- Row 3: Repetitions --
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3; // Span across all columns
        chkEnableLoop = new JCheckBox("Enable Loop");
        panel.add(chkEnableLoop, gbc);

        // -- Row 4: Status Label--
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        lblStatusLabel = new JLabel("Load an audio file to begin.", SwingConstants.CENTER);
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
        // Main container for all controls at the bottom
        JPanel pnlControlContainer = new JPanel(new BorderLayout());
        pnlControlContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add the timeline and the buttons to this container
        pnlControlContainer.add(createTimelinePanel(), BorderLayout.NORTH);
        pnlControlContainer.add(createButtonPanel(), BorderLayout.CENTER);

        return pnlControlContainer;
    }

    /**
     * Creates the panel containing the Play, Pause, and Stop buttons.
     *
     * @return A JPanel with the playback action buttons.
     */
    private JPanel createButtonPanel() {
        // Panel for the buttons, using FlowLayout to center them
        JPanel pnlButtonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        btnPlay = new JButton("Play");
        btnPause = new JButton("Pause");
        btnStop = new JButton("Stop");

        //Add Action Listeners
        btnPlay.addActionListener(e -> playAudio());
        btnPause.addActionListener(e -> pauseAudio());
        btnStop.addActionListener(e -> stopAudio());

        // Set preferred sizes to make buttons uniform
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
        // Panel for the timeline slider and labels
        JPanel pnlTimeline = new JPanel(new BorderLayout(10, 0));

        // Labels to show current time and total duration
        lblStartTime = new JLabel("0:00");
        lblEndTime = new JLabel("0:00"); // Placeholder duration

        // The slider to represent the song's progress
        // Set the min/max values dynamically when a song is loaded.
        sldrTimelineSlider = new JSlider(0, 244); // 4 minutes * 60 + 4 seconds = 244
        sldrTimelineSlider.setValue(0);

        // A Listener to handle user clicking and dragging the slider
        sldrTimelineSlider.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override
            public void mousePressed(java.awt.event.MouseEvent e){
                boolIsUserDragging = true; // User has taken control
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e){
                if (clpAudioClip != null){
                    int intNewPosition = sldrTimelineSlider.getValue();
                    //Jump the audio to the new position (converting sec back to ms)
                    clpAudioClip.setMicrosecondPosition(intNewPosition * 1_000_000L);
                }
                boolIsUserDragging = false; // User has released control
            }
        });

        // A Listener to update the time label WHILE dragging
        sldrTimelineSlider.addChangeListener(e ->{
            if(boolIsUserDragging){
                lblStartTime.setText(formatTime(sldrTimelineSlider.getValue()));
            }
        });

        pnlTimeline.add(lblStartTime, BorderLayout.WEST);
        pnlTimeline.add(sldrTimelineSlider, BorderLayout.CENTER);
        pnlTimeline.add(lblEndTime, BorderLayout.EAST);

        return pnlTimeline;
    }

    /**
     * Sets up the Swing Timer to update the GUI every second during playback.
     */
    private void setupTimer() {
        tmrTimeline = new Timer(50, e -> {
            if (clpAudioClip != null && clpAudioClip.isRunning()) {
                long currentMicroSeconds = clpAudioClip.getMicrosecondPosition();
                // Only update the slider pos if the user isn't dragging it
                if (!boolIsUserDragging){
                    long currentSeconds = currentMicroSeconds / 1_000_000;
                    sldrTimelineSlider.setValue((int) currentSeconds);
                    lblStartTime.setText(formatTime(currentMicroSeconds));
                }


                // -- Core Looping Logic --
                if (chkEnableLoop.isSelected()) {
                    long loopEndMicro = parseTime(txtLoopEnd.getText());

                    if (currentMicroSeconds >= loopEndMicro) {
                        if (intRepeatsRemaining > 0) {
                            intRepeatsRemaining--;
                            LOGGER.log(Level.INFO, "Looping. Repeats Remaining: {0}", intRepeatsRemaining);
                            long loopStartMicro = parseTime(txtLoopStart.getText());
                            clpAudioClip.setMicrosecondPosition(loopStartMicro);
                        } else {
                            // The loop has finished, let the song continue
                            chkEnableLoop.setSelected(false);
                            LOGGER.log(Level.INFO, "Looping has finished. Playing remainder of the song...");
                        }
                    }
                }
            }
        });
    }

    /**
     * Loads the selected audio file into the player.
     * @param fileToLoad The audio file to load.
     */
    private void loadAudioFile(File fileToLoad) {
        try {
            // If a clip is already open, close it to free up resources.
            if (clpAudioClip != null) {
                clpAudioClip.close();
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileToLoad);
            clpAudioClip = AudioSystem.getClip();

            // Adds a listener to handle events like STOP (when a song ends)
            clpAudioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    // When naturally finishing, stop the timer and reset UI
                    if (clpAudioClip.getMicrosecondLength() == clpAudioClip.getMicrosecondPosition()) {
                        stopAudio();
                    }
                }
            });

            clpAudioClip.open(audioStream);

            // Update GUI with new audio file info
            long durationMicroseconds = clpAudioClip.getMicrosecondLength();
            long durationSeconds = durationMicroseconds / 1_000_000;
            sldrTimelineSlider.setMaximum((int) durationSeconds);
            lblEndTime.setText(formatTime(durationMicroseconds));
            lblStatusLabel.setText("Loaded: " + fileToLoad.getName());

            stopAudio(); // Reset player to a clean state

            setPlaybackButtonsEnabled(true);
            setLoopControlsEnabled(true);

            LOGGER.log(Level.INFO, "Successfully loaded audio file: {0}", fileToLoad.getAbsolutePath());

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            LOGGER.log(Level.SEVERE, "Error loading audio file", e);
            JOptionPane.showMessageDialog(frmFoundation,
                    "Could not load the audio file: " + e.getMessage(),
                    "Audio Load Error",
                    JOptionPane.ERROR_MESSAGE);
            setPlaybackButtonsEnabled(false);
            setLoopControlsEnabled(false);
        }
    }

    /**
     * Starts playback of currently loaded audio clip
     */
    private void playAudio() {
        if (clpAudioClip != null) {
            // If loop is enabled, start the repeat counter
            if (chkEnableLoop.isSelected()) {
                try {
                    intRepeatsRemaining = Integer.parseInt(txtLoopCount.getText());
                    LOGGER.log(Level.INFO, "Starting loop with {0} repetitions.", intRepeatsRemaining);
                } catch (NumberFormatException r) {
                    LOGGER.log(Level.WARNING, "Invalid Repeat count. Defaulting to 1");
                    intRepeatsRemaining = 1;
                    txtLoopCount.setText("1");
                }
            }
            clpAudioClip.start();
            tmrTimeline.start();
            btnPlay.setText("Resume");
            LOGGER.info("Playback BEGIN");
        }
    }

    /**
     * Pauses currently loaded audio clip
     */
    private void pauseAudio() {
        if (clpAudioClip != null && clpAudioClip.isRunning()) {
            clpAudioClip.stop();
            tmrTimeline.stop();
            LOGGER.info("Playback PAUSED");
        }
    }

    /**
     * Stops currently loaded audio clip
     */
    private void stopAudio() {
        if (clpAudioClip != null) {
            // Stop the clip first
            clpAudioClip.stop();
            // Reset its position to the beginning
            clpAudioClip.setFramePosition(0);
            // Stop the timer that updates the slider
            tmrTimeline.stop();
            // Reset the slider and time label to the start
            sldrTimelineSlider.setValue(0);
            lblStartTime.setText("0:00");
            btnPlay.setText("Play");
            // Reset the Loop state
            chkEnableLoop.setSelected(false);
            //loopRepetitionRemaining = 0;
            LOGGER.info("Playback stopped and reset.");
        }
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
    }

    /**
     * Sets the text of a target field to the current time on the timeline
     *
     * @param targetField The JTextField to target
     */
    private void setLoopPoint(JTextField targetField) {
        if (clpAudioClip != null) {
            long currentMicroSeconds = clpAudioClip.getMicrosecondPosition();
            targetField.setText(formatTime(currentMicroSeconds));
        }
    }

    /**
     * Parses a time string (MM:SS) into total seconds
     *
     * @param timeString The String to parse
     * @return The total number of seconds
     */
    private long parseTime(String timeString) {
        try {
            String[] parts = timeString.split(":");
            if (parts.length == 2) {
                long minutes = Long.parseLong(parts[0]);
                long seconds = 0;
                long milliseconds = 0;

                String secondPart = parts[1];
                if (secondPart.contains(".")) {
                    String[] secParts = secondPart.split("\\.");
                    seconds = Long.parseLong(secParts[0]);

                    // Handle user-typed milliseconds of varying length (e.g., .9, .90, .900)
                    String msString = secParts[1];
                    if (msString.length() > 3) { // Truncate if too long
                        msString = msString.substring(0, 3);
                    }
                    while(msString.length() < 3) { // Pad with zeros if too short
                        msString += "0";
                    }
                    milliseconds = Long.parseLong(msString);

                } else {
                    // No milliseconds, just parse the seconds
                    seconds = Long.parseLong(secondPart);
                }

                // Convert everything to microseconds
                return (minutes * 60 * 1_000_000L) + (seconds * 1_000_000L) + (milliseconds * 1000L);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not parse time format: " + timeString, e);
        }
        return -1; // Return -1 on failure to prevent accidental looping at 0
    }

    /**
     * Formats a duration in total seconds to an MM:SS string
     *
     * @param totalMicroSeconds The duration in Seconds
     * @return A properly formatted string
     */
    private String formatTime(long totalMicroSeconds) {
        long totalSeconds = totalMicroSeconds / 1_000_000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long milliseconds = (totalMicroSeconds / 1000) % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds,milliseconds);
    }

    /**
     * A custom display for file names in the browser, and not the full path
     */
    class FlieNameRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus){
                super.getListCellRendererComponent(list,value,index,isSelected,hasFocus);
                if (value instanceof File){
                    File file = (File) value;
                    setText(file.getName()); // Only show the name
                }
            return this;
        }
    }

    /**
     * Opens a JFileChooser to select a directory, then populates the file list
     * with supported audio files found inside
     */
    private void openFolder(){
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setDialogTitle("Select Audio Folder");
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //Only allow folders to be selected
        folderChooser.setAcceptAllFileFilterUsed(false); //Disables "All Files" option;

        if (folderChooser.showOpenDialog(frmFoundation) == JFileChooser.APPROVE_OPTION){
            File selectedFolder = folderChooser.getSelectedFile();

            //Clear out the old list
            fileListModel.clear();

            //Find all .wav and .au files within the selected folder
            File[] audioFiles = selectedFolder.listFiles((dir,name)->
                    name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".au")
            );

            if (audioFiles != null){
                for (File file : audioFiles){
                    fileListModel.addElement(file);
                }
                LOGGER.log(Level.INFO, "Found {0} audio files in {1}",new Object[]{audioFiles.length, selectedFolder.getAbsolutePath()});
            }
        }
    }
}
