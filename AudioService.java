import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
}
