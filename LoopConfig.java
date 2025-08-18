/**
 * A simple data class to hold the loop configuration for a single audio file.
 * Now includes methods for converting to and from a savable string format.
 */
public class LoopConfig {
    String loopStart = "00:00.000";
    String loopEnd = "00:00.000";
    int repeats = 1;
    boolean isInfinite = false;

    /**
     * Converts the configuration into a single string for saving to a file.
     * Format: loopStart|loopEnd|repeats|isInfinite
     * @return A savable string representation of the config.
     */
    @Override
    public String toString() {
        return String.join("|", loopStart, loopEnd, String.valueOf(repeats), String.valueOf(isInfinite));
    }

    /**
     * Creates a new LoopConfig by parsing a string from a file.
     * @param fromString The string loaded from the properties file.
     * @return A new LoopConfig object.
     */
    public static LoopConfig fromString(String fromString) {
        LoopConfig config = new LoopConfig();
        try {
            String[] parts = fromString.split("\\|");
            if (parts.length == 4) {
                config.loopStart = parts[0];
                config.loopEnd = parts[1];
                config.repeats = Integer.parseInt(parts[2]);
                config.isInfinite = Boolean.parseBoolean(parts[3]);
            }
        } catch (Exception e) {
            // If parsing fails, just return a default config.
            System.err.println("Failed to parse loop config string: " + fromString);
        }
        return config;
    }
}
