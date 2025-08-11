import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Driver;
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
    private JFrame frmFoundation;
    private JLabel lblStatusLabel;
    private JLabel lblStartTime;
    private JLabel lblEndTime;
    private JSlider sldrTimelineSlider;
    private JButton btnPlay, btnPause, btnStop, btnLoad;

    // -- Loop Control Components --
    private JTextField txtLoopStart, txtLoopEnd, txtLoopCount;
    private JButton btnSetLoopStart, btnSetLoopEnd;
    private JCheckBox chkEnableLoop;


    //A storage area for loaded audio data
    private Clip clpAudioClip;
    // Timer for updating timeline slider
    private Timer tmrTimeline;

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
        frmFoundation.add(createTopPanel(), BorderLayout.NORTH);
        frmFoundation.add(createLoopControlsPanel(), BorderLayout.CENTER);
        frmFoundation.add(createPlaybackControlsPanel(), BorderLayout.SOUTH);


        lblStatusLabel = new JLabel("Load an audio file to begin");
        lblStatusLabel.add(createTopPanel(), BorderLayout.NORTH);
        lblStatusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initial state for controls
        setPlaybackButtonsEnabled(false);
        setLoopControlsEnabled(false);

        // Initialize the Timer
        setupTimer();

        // Size the window and make it visible
        frmFoundation.pack(); // Sizes the window to fit the preferred size of its subcomponents
        frmFoundation.setMinimumSize(frmFoundation.getSize()); // Prevent resizing smaller than packed size
        frmFoundation.setLocationRelativeTo(null); // Center the window on the screen
        frmFoundation.setVisible(true);
    }

    /**
     * Creates the top panel which contains the file loading controls.
     *
     * @return A JPanel containing the load/unload button.
     */
    private JPanel createTopPanel() {
        JPanel pnlTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTopPanel.setBorder(new EmptyBorder(5, 5, 0, 5)); // Add some padding

        btnLoad = new JButton("Load Audio File");
        btnLoad.addActionListener(e -> loadAudioFile());
        pnlTopPanel.add(btnLoad);

        return pnlTopPanel;
    }

    /**
     * Creates the Central Panel for loop settings
     * @return A JPanel containing all loop control components
     */
    private JPanel createLoopControlsPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Loop Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        // -- Row 0: Loop Start --
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Loop Start: "),gbc);

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

        pnlTimeline.add(lblStartTime, BorderLayout.WEST);
        pnlTimeline.add(sldrTimelineSlider, BorderLayout.CENTER);
        pnlTimeline.add(lblEndTime, BorderLayout.EAST);

        return pnlTimeline;
    }

    /**
     * Sets up the Swing Timer to update the GUI every second during playback.
     */
    private void setupTimer() {
        tmrTimeline = new Timer(1000, e -> {
            if (clpAudioClip != null && clpAudioClip.isRunning()) {
                long currentSeconds = clpAudioClip.getMicrosecondPosition() / 1_000_000;
                sldrTimelineSlider.setValue((int) currentSeconds);
                lblStartTime.setText(formatTime(currentSeconds));
            }
        });
    }

    /**
     * Opens a JFileChooser dialog to allow user to select an Audio File
     * For now, once selected, it'll just print to the console
     */
    private void loadAudioFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Audio File (.mp3, .wav, etc)");
        //Filter for common audio types [STANDARD JAVA ONLY SUPPORT .wav / .au OUT OF THE BOX]
        fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files", "wav", "mp3", "au"));

        int usrSelection = fileChooser.showOpenDialog(frmFoundation);

        if (usrSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                //If a clip is already opened, close it to free resources
                if (clpAudioClip != null && clpAudioClip.isOpen()) {
                    clpAudioClip.close();
                }

                AudioInputStream strmAudioStream = AudioSystem.getAudioInputStream(selectedFile);
                clpAudioClip = AudioSystem.getClip();

                // Adds a listener to handle events like STOP (when a song ends)
                clpAudioClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP){
                        // When naturally finishing, stop the timer and reset UI
                        if (clpAudioClip.getMicrosecondLength() == clpAudioClip.getMicrosecondPosition()){
                            stopAudio();
                        }
                    }
                });

                clpAudioClip.open(strmAudioStream);

                // Update GUI with new audio file info
                lblStatusLabel.setText("Loaded: " + selectedFile.getName());
                long durationSeconds = clpAudioClip.getMicrosecondLength() / 1_000_000;
                sldrTimelineSlider.setMaximum((int) durationSeconds);
                sldrTimelineSlider.setValue(0);
                lblStartTime.setText("0:00");
                lblEndTime.setText(formatTime(durationSeconds));
                setPlaybackButtonsEnabled(true);
                setLoopControlsEnabled(true);
                btnPlay.setText("Play");

                LOGGER.log(Level.INFO, "Successfully loaded audio file: {0}", selectedFile.getAbsolutePath());
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                LOGGER.log(Level.SEVERE, "Error loading audio file", e);
                JOptionPane.showMessageDialog(frmFoundation,
                        "Could not load the audio file: " + e.getMessage(),
                        "Audio Load Error",
                        JOptionPane.ERROR_MESSAGE);
                setPlaybackButtonsEnabled(false);
                setLoopControlsEnabled(false);
            }


            System.out.println("Selected File: " + selectedFile.getAbsolutePath());
            lblStatusLabel.setText("Loaded: " + selectedFile.getName());
        }
    }

    /**
     * Starts playback of currently loaded audio clip
     */
    private void playAudio() {
        if (clpAudioClip != null) {
            // If loop is enabled, start the repeat counter
            if (chkEnableLoop.isSelected()){
                try{
                //TODO: BEGIN HERE!
                } catch (NumberFormatException r){
                    LOGGER.log(Level.WARNING, "Invalid Repeat count. Defaulting to 1");

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
     * @param enabled true to enable, false to disable
     */
    private void setLoopControlsEnabled(boolean enabled){
        txtLoopStart.setEnabled(enabled);
        txtLoopEnd.setEnabled(enabled);
        txtLoopCount.setEnabled(enabled);
        btnSetLoopStart.setEnabled(enabled);
        btnSetLoopEnd.setEnabled(enabled);
        chkEnableLoop.setEnabled(enabled);
    }

    /**
     * Sets the text of a target field to the current time on the timeline
     * @param targetField The JTextField to target
     */
    private void setLoopPoint(JTextField targetField){
        if (clpAudioClip != null){
            long currnetTimeSeconds = sldrTimelineSlider.getValue();
            targetField.setText(formatTime(currnetTimeSeconds));
        }
    }

    /**
     * Parses a time string (MM:SS) into total seconds
     * @param timeString The String to parse
     * @return The total number of seconds
     */
    private long parseTime(String timeString){
        try{
            String[] parts = timeString.split(":");
            if (parts.length == 2){
                long minutes = Long.parseLong(parts[0]);
                long seconds = Long.parseLong(parts[1]);
                return (minutes * 60) + seconds;
            }
        } catch (NumberFormatException e){
            LOGGER.log(Level.WARNING, "Invalid Time Format: " + timeString,e);
        }
        return 0; // If parsing fails, return 0 as the default
    }

    /**
     * Formats a duration in total seconds to a MM:SS string
     *
     * @param totalSeconds The duration in Seconds
     * @return A properly formatted string
     */
    private String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
