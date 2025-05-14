// ParsedScript.java

import java.util.Set;

public class ParsedScript {
    private final String act;
    private final String scene;
    private final String title;
    private final String currentMusicFile;
    private final Set<String> sfxCues;
    private final Set<String> clickableTerms;
    private final Set<String> musicCues;
    private final String text;

    public ParsedScript(
            String act,
            String scene,
            String title,
            String currentMusicFile,
            Set<String> sfxCues,
            Set<String> clickableTerms,
            Set<String> musicCues,
            String text
    ) {
        this.act               = act;
        this.scene             = scene;
        this.title             = title;
        this.currentMusicFile  = currentMusicFile;
        this.sfxCues           = sfxCues;
        this.clickableTerms    = clickableTerms;
        this.musicCues         = musicCues;
        this.text              = text;
    }

    public String getAct() {
        return act;
    }

    public String getScene() {
        return scene;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Returns the raw filename (e.g. "BattleTheme.mp3") from the last MUSIC tag.
     */
    public String getMusicFile() {
        return currentMusicFile;
    }

    public Set<String> getSfxCues() {
        return sfxCues;
    }

    public Set<String> getClickableTerms() {
        return clickableTerms;
    }

    /**
     * The bracketed display names of music cues (e.g. "Battle Theme").
     */
    public Set<String> getMusicCues() {
        return musicCues;
    }

    /**
     * The fully cleaned script text, with placeholders for terms and music cues.
     */
    public String getText() {
        return text;
    }
}
