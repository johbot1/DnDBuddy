import javax.sound.sampled.*;
import javax.swing.Timer;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioService {
    // A logger for logging messages for this class
    private static final Logger LOGGER = Logger.getLogger(MusicLooperGUI.class.getName());
    //A storage area for loaded audio data
    private Clip clpAudioClip;
    // Timer for updating timeline slider
    private Timer tmrTimeline;
    //Counter for repeats
    private int intRepeatsRemaining;

    // -- Configuration Components --
    private final Map<File, LoopConfig> loopConfigMap = new HashMap<>();
    private File currentlyLoadedFile;

    // -- Callback + Providers from GUI --
    private final Consumer<Long> onTimeUpdate;
    private final Supplier<LoopConfig> loopConfigProvider;
    private final Supplier<Boolean> isLoopEnabledProvider;
    private final Runnable onLoopFinishCallback;

    /**
     * Constructor for the AudioService.
     * @param onTimeUpdate A function that will be called every timer tick with the current microsecond position.
     * @param loopConfigProvider A function that provides the current loop settings from the UI.
     * @param isLoopEnabledProvider A function that returns true if the loop checkbox is enabled.
     * @param onLoopFinishCallback A function to call when the looping finishes.
     */
    public AudioService(Consumer<Long> onTimeUpdate, Supplier<LoopConfig> loopConfigProvider, Supplier<Boolean> isLoopEnabledProvider, Runnable onLoopFinishCallback) {
        this.onTimeUpdate = onTimeUpdate;
        this.loopConfigProvider = loopConfigProvider;
        this.isLoopEnabledProvider = isLoopEnabledProvider;
        this.onLoopFinishCallback = onLoopFinishCallback;
        this.setupTimer();
    }
    /**
     * Sets up the Swing Timer to update the GUI every second during playback.
     */
    private void setupTimer() {
        tmrTimeline = new Timer(50, e -> {
            if (clpAudioClip != null && clpAudioClip.isRunning()) {
                long currentMicroSeconds = clpAudioClip.getMicrosecondPosition();

                onTimeUpdate.accept(currentMicroSeconds);

//                // Only update the slider pos if the user isn't dragging it
//                if (!boolIsUserDragging) {
//                    long currentSeconds = currentMicroSeconds / 1_000_000;
//                    sldrTimelineSlider.setValue((int) currentSeconds);
//                    lblStartTime.setText(formatTime(currentMicroSeconds));
//                }


                // Check if looping is enabled by calling the provider function
                if (isLoopEnabledProvider.get()) {
                    // Get the current settings from the GUI via the provider
                    LoopConfig currentConfig = loopConfigProvider.get();
                    long loopEndMicro = parseTime(currentConfig.loopEnd);

                    if (currentMicroSeconds >= loopEndMicro) {
                        if (intRepeatsRemaining > 0) {
                            intRepeatsRemaining--;
                            LOGGER.log(Level.INFO, "Looping. Repeats Remaining: {0}", intRepeatsRemaining);
                            long loopStartMicro = parseTime(currentConfig.loopStart);
                            clpAudioClip.setMicrosecondPosition(loopStartMicro);
                        } else {
                            // Tell the GUI to uncheck the box
                            onLoopFinishCallback.run();
                            LOGGER.log(Level.INFO, "Looping has finished. Playing remainder of the song...");
                        }
                    }
                }
            }
        });
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
                    while (msString.length() < 3) { // Pad with zeros if too short
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
    public static String formatTime(long totalMicroSeconds) {
        long totalSeconds = totalMicroSeconds / 1_000_000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long milliseconds = (totalMicroSeconds / 1000) % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
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
}
