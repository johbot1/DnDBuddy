import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MusicLooperGUI sets up the main graphical user interface for the music looper application.
 * It includes playback controls, a timeline slider, and a button to load audio files.
 * This initial version focuses only on the layout and component placement.
 */
public class MusicLooperGUI {

    private JFrame frmFoundation;
    private JLabel lblStatusLabel;
    private JLabel  lblStartTime;
    private JLabel lblEndTime;
    private JSlider sldrTimelineSlider;
    private JButton btnPlay, btnPause, btnStop, btnLoad;
    // A logger for logging messages for this class
    private static final Logger LOGGER = Logger.getLogger(MusicLooperGUI.class.getName());

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
        // 1. Create the main window (JFrame)
        frmFoundation = new JFrame("Groove Buddy - Music Looper");
        frmFoundation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmFoundation.setLayout(new BorderLayout(10, 10)); // Use BorderLayout for overall structure

        // 2. Add all the panels to the frame
        frmFoundation.add(createTopPanel(), BorderLayout.NORTH);
        frmFoundation.add(createPlaybackControlsPanel(), BorderLayout.SOUTH);

        // A placeholder in the center. You could put song info or album art here later.
        lblStatusLabel = new JLabel("Load an audio file to begin");
        lblStatusLabel.add(createTopPanel(), BorderLayout.NORTH);
        lblStatusLabel.setBorder(new EmptyBorder(10,10,10,10));
        frmFoundation.add(lblStatusLabel, BorderLayout.CENTER);


        // 3. Size the window and make it visible
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
        // We'll set the min/max values dynamically when a song is loaded.
        sldrTimelineSlider = new JSlider(0, 244); // 4 minutes * 60 + 4 seconds = 244
        sldrTimelineSlider.setValue(0);

        pnlTimeline.add(lblStartTime, BorderLayout.WEST);
        pnlTimeline.add(sldrTimelineSlider, BorderLayout.CENTER);
        pnlTimeline.add(lblEndTime, BorderLayout.EAST);

        return pnlTimeline;
    }

    /**
     * Opens a JFileChooser dialog to allow user to select an Audio File
     * For now, once selected, it'll just print to the console
     */
    private void loadAudioFile(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Audio File (.mp3, .wav, etc)");
        //Filter for common audio types
        fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files", "wav", "mp3","au"));

        int usrSelection = fileChooser.showOpenDialog(frmFoundation);

        if (usrSelection == JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Selected File: " + selectedFile.getAbsolutePath());
            lblStatusLabel.setText("Loaded: " + selectedFile.getName());
        }
    }
}
