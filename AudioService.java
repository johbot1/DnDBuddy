import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

import javax.sound.sampled.*;
import javax.swing.Timer;
import java.io.*;
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

            if (fileName.endsWith(".mp3")) {
                audioStream = convertMp3ToAudioInputStream(fileToLoad);
            } else {
                // For WAV, AU, etc., use the standard method
                audioStream = AudioSystem.getAudioInputStream(fileToLoad);
            }

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
     * Converts an MP3 file to a PCM AudioInputStream using JLayer
     * @param mp3File The MP3 file to convert
     * @return AudioInputStream containing decoded PCM data
     * @throws Exception if conversion fails
     */
    private AudioInputStream convertMp3ToAudioInputStream(File mp3File) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(mp3File);
        Bitstream bitstream = new Bitstream(fileInputStream);
        Decoder decoder;

        // Read first frame to get format information
        Header firstHeader = bitstream.readFrame();
        if (firstHeader == null) {
            throw new Exception("Invalid MP3 file - no frames found");
        }

        // Create audio format based on MP3 header
        int channels = (firstHeader.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
        float sampleRate = firstHeader.frequency();
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16, // 16-bit
                channels,
                channels * 2, // frame size (2 bytes per sample * channels)
                sampleRate, // frame rate
                false // little endian
        );

        // Decode the entire MP3 to PCM data
        ByteArrayOutputStream pcmOutput = new ByteArrayOutputStream();

        // Reset the bitstream to start from beginning
        bitstream.close();
        fileInputStream.close();
        fileInputStream = new FileInputStream(mp3File);
        bitstream = new Bitstream(fileInputStream);
        decoder = new Decoder();

        Header header;
        int frameCount = 0;
        while ((header = bitstream.readFrame()) != null) {
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);

            if (output != null) {
                // Convert samples to byte array
                short[] samples = output.getBuffer();
                int sampleCount = output.getBufferLength();

                for (int i = 0; i < sampleCount; i++) {
                    // Write as little-endian 16-bit signed PCM
                    int sample = samples[i];
                    pcmOutput.write(sample & 0xFF);        // Low byte
                    pcmOutput.write((sample >> 8) & 0xFF); // High byte
                }
            }

            bitstream.closeFrame();
            frameCount++;
        }

        bitstream.close();
        fileInputStream.close();

        if (frameCount == 0) {
            throw new Exception("No valid MP3 frames found");
        }

        byte[] pcmData = pcmOutput.toByteArray();
        ByteArrayInputStream pcmInput = new ByteArrayInputStream(pcmData);

        return new AudioInputStream(pcmInput, decodedFormat, pcmData.length / decodedFormat.getFrameSize());
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
                long seconds;
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