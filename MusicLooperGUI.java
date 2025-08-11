import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * MusicLooperGUI sets up the main graphical user interface for the music looper application.
 * It includes playback controls, a timeline slider, and a button to load audio files.
 * This initial version focuses only on the layout and component placement.
 */
public class MusicLooperGUI {

    private JFrame frame;

    /**
     * The main entry point for the application.
     * It schedules the GUI creation on the Event Dispatch Thread.
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
                e.printStackTrace();
            }
        });
    }

    /**
     * Initializes the main frame and all its UI components.
     */
    private void initUI() {
        // 1. Create the main window (JFrame)
        frame = new JFrame("Groove Buddy - Music Looper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10)); // Use BorderLayout for overall structure

        // 2. Add all the panels to the frame
        frame.add(createTopPanel(), BorderLayout.NORTH);
        frame.add(createPlaybackControlsPanel(), BorderLayout.SOUTH);

        // A placeholder in the center. You could put song info or album art here later.
        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(new EmptyBorder(10,10,10,10));
        centerPanel.add(new JLabel("Load an audio file to begin."));
        frame.add(centerPanel, BorderLayout.CENTER);


        // 3. Size the window and make it visible
        frame.pack(); // Sizes the window to fit the preferred size of its subcomponents
        frame.setMinimumSize(frame.getSize()); // Prevent resizing smaller than packed size
        frame.setLocationRelativeTo(null); // Center the window on the screen
        frame.setVisible(true);
    }

    /**
     * Creates the top panel which contains the file loading controls.
     * @return A JPanel containing the load/unload button.
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(new EmptyBorder(5, 5, 0, 5)); // Add some padding

        JButton loadButton = new JButton("Load Audio File");
        // We will add an ActionListener here later to open a file chooser.
        topPanel.add(loadButton);

        return topPanel;
    }

    /**
     * Creates the main panel for all playback controls, including the timeline and buttons.
     * This panel will be placed at the bottom of the window.
     * @return A JPanel containing the timeline and playback buttons.
     */
    private JPanel createPlaybackControlsPanel() {
        // Main container for all controls at the bottom
        JPanel controlsContainer = new JPanel(new BorderLayout());
        controlsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add the timeline and the buttons to this container
        controlsContainer.add(createTimelinePanel(), BorderLayout.NORTH);
        controlsContainer.add(createButtonPanel(), BorderLayout.CENTER);

        return controlsContainer;
    }

    /**
     * Creates the panel containing the Play, Pause, and Stop buttons.
     * @return A JPanel with the playback action buttons.
     */
    private JPanel createButtonPanel() {
        // Panel for the buttons, using FlowLayout to center them
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton playButton = new JButton("Play");
        JButton pauseButton = new JButton("Pause");
        JButton stopButton = new JButton("Stop");

        // Set preferred sizes to make buttons uniform
        Dimension buttonSize = new Dimension(80, 30);
        playButton.setPreferredSize(buttonSize);
        pauseButton.setPreferredSize(buttonSize);
        stopButton.setPreferredSize(buttonSize);

        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);

        return buttonPanel;
    }

    /**
     * Creates the panel for the audio timeline, including the slider and time labels.
     * @return A JPanel with the JSlider and time labels.
     */
    private JPanel createTimelinePanel() {
        // Panel for the timeline slider and labels
        JPanel timelinePanel = new JPanel(new BorderLayout(10, 0));

        // Labels to show current time and total duration
        JLabel startTimeLabel = new JLabel("0:00");
        JLabel endTimeLabel = new JLabel("4:04"); // Placeholder duration

        // The slider to represent the song's progress
        // We'll set the min/max values dynamically when a song is loaded.
        JSlider timelineSlider = new JSlider(0, 244); // 4 minutes * 60 + 4 seconds = 244
        timelineSlider.setValue(0);

        timelinePanel.add(startTimeLabel, BorderLayout.WEST);
        timelinePanel.add(timelineSlider, BorderLayout.CENTER);
        timelinePanel.add(endTimeLabel, BorderLayout.EAST);

        return timelinePanel;
    }
}
