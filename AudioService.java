import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;

import javax.sound.sampled.*;
import javax.swing.Timer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
     * The information pertaining to a specified Audio file
     *
     * @param durationMicroseconds How long the file lasts in microseconds
     * @param config               Any applied loop configuration
     */
    public record AudioDetails(long durationMicroseconds, LoopConfig config) {}

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
     * Starts the timeline, and the audio stream simultaneously
     */
    public void play() {
        if (clpAudioClip != null) {
            if (isLoopEnabledProvider.get() && !tmrTimeline.isRunning()) {
                LoopConfig config = loopConfigProvider.get();
                intRepeatsRemaining = config.repeats;
                LOGGER.log(Level.INFO, "Starting loop with {0} repetitions.", intRepeatsRemaining);
            }
            clpAudioClip.start();
            tmrTimeline.start();
            LOGGER.info("Playback BEGIN");
        }
    }

    /**
     * Halts both the timeline and the audio stream
     */
    public void pause() {
        if (clpAudioClip != null && clpAudioClip.isRunning()) {
            clpAudioClip.stop();
            tmrTimeline.stop();
            LOGGER.info("Playback PAUSED");
        }
    }

    /**
     * Stops the currently playing audio file
     */
    public void stop() {
        if (clpAudioClip != null) {
            clpAudioClip.stop();
            clpAudioClip.setFramePosition(0);
            tmrTimeline.stop();
            onTimeUpdate.accept(0L); // Tell GUI to reset its time display to 0
            intRepeatsRemaining = 0;
            LOGGER.info("Playback stopped and reset.");
        }
    }

    /**
     * Allows a user to use the timeline to scrub through the loaded audio file
     * @param microseconds The play head position described in microseconds
     */
    public void seek(long microseconds) {
        if (clpAudioClip != null) {
            clpAudioClip.setMicrosecondPosition(microseconds);
        }
    }

    /**
     * Grabs the current microsecond position of a playing audio file
     * @return The microsecond position, or 0 if there's an issue with the file
     */
    public long getCurrentMicroseconds() {
        if (clpAudioClip != null) {
            return clpAudioClip.getMicrosecondPosition();
        }
        return 0;
    }

    // -- FILE + CONFIG Logic
    /**
     * Loads the selected audio file, retrieving its loop config if it exists.
     * @param fileToLoad The file to load.
     * @return An AudioDetails object on success, or null on failure.
     */
    public AudioDetails loadFile(File fileToLoad) {
        this.currentlyLoadedFile = fileToLoad;
        try {
            if (clpAudioClip != null) clpAudioClip.close();

            AudioInputStream audioStream;
            String fileName = fileToLoad.getName().toLowerCase();

            // --- THIS IS THE NEW LOGIC ---
            if (fileName.endsWith(".mp3")) {
                // Manually decode the MP3 to a PCM AudioInputStream
                InputStream fileInputStream = new FileInputStream(fileToLoad);
                Bitstream bitstream = new Bitstream(fileInputStream);
                Header header = bitstream.readFrame();

                // Get audio format from MP3 header
                int channels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
                AudioFormat decodedFormat = new AudioFormat(
                        header.frequency(),
                        16, // Bit depth
                        channels,
                        true, // Signed
                        false // Big-endian
                );

                // Create an AudioInputStream from the decoded MP3 stream
                audioStream = new AudioInputStream(fileInputStream, decodedFormat, -1);

            } else {
                // For WAV, AU, etc., use the standard method
                audioStream = AudioSystem.getAudioInputStream(fileToLoad);
            }
            // --- END OF NEW LOGIC ---

            clpAudioClip = AudioSystem.getClip();

            clpAudioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && clpAudioClip.getMicrosecondLength() == clpAudioClip.getMicrosecondPosition()) {
                    stop();
                }
            });

            clpAudioClip.open(audioStream);
            LOGGER.log(Level.INFO, "Successfully loaded audio file: {0}", fileToLoad.getAbsolutePath());
            LoopConfig config = loopConfigMap.computeIfAbsent(fileToLoad, k -> new LoopConfig());
            return new AudioDetails(clpAudioClip.getMicrosecondLength(), config);

        } catch (Exception e) { // Catching generic Exception as JLayer throws some
            LOGGER.log(Level.SEVERE, "Error loading audio file", e);
            this.currentlyLoadedFile = null;
            return null;
        }
    }

    /**
     * Saves the current UI loop settings to the in-memory map.
     * @param config The new configuration to save for the current file.
     */
    public void updateCurrentConfig(LoopConfig config) {
        if (currentlyLoadedFile != null) {
            loopConfigMap.put(currentlyLoadedFile, config);
        }
    }

    // -- Utility Methods --
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
                    String msString = secParts[1];
                    if (msString.length() > 3) msString = msString.substring(0, 3);
                    while (msString.length() < 3) msString += "0";
                    milliseconds = Long.parseLong(msString);
                } else {
                    seconds = Long.parseLong(secondPart);
                }
                return (minutes * 60 * 1_000_000L) + (seconds * 1_000_000L) + (milliseconds * 1000L);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not parse time format: " + timeString, e);
        }
        return -1;
    }

    /**
     * Formats a duration in total seconds to an MM:SS string
     *
     * @param totalMicroSeconds The duration in Seconds
     * @return A properly formatted string
     */
    public String formatTime(long totalMicroSeconds) {
        long totalSeconds = totalMicroSeconds / 1_000_000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long milliseconds = (totalMicroSeconds / 1000) % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
    }
}
