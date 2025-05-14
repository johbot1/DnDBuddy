import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class ScriptParser {
    // Matches: ACT 1 SCENE 2 – Arrival...
    private static final Pattern HEADER_PATTERN =
            Pattern.compile("^ACT\\s+(\\d+)\\s+SCENE\\s+(\\d+)\\s+[-–]\\s+(.+)$");
    // <<MUSIC: filename.mp3>>
    private static final Pattern MUSIC_PATTERN =
            Pattern.compile("<<MUSIC:\\s*([^>]+)>>");
    // <<SFX: cue_name>>
    private static final Pattern SFX_PATTERN =
            Pattern.compile("<<SFX:\\s*([^>]+)>>");
    // {Clickable Term}
    private static final Pattern TERM_PATTERN =
            Pattern.compile("\\{([^}]+)\\}");

    public static ParsedScript parse(Path scriptFile) throws IOException {
        List<String> lines = Files.readAllLines(scriptFile);

        int act = 0, scene = 0;
        String title = "";
        String musicFile = null;
        Set<String> sfxCues = new LinkedHashSet<>();
        Set<String> clickableTerms = new LinkedHashSet<>();
        StringBuilder cleaned = new StringBuilder();

        boolean headerFound = false;
        for (String raw : lines) {
            String line = raw;

            // 1) Parse header only once
            if (!headerFound && !line.trim().isEmpty()) {
                Matcher hm = HEADER_PATTERN.matcher(line.trim());
                if (hm.matches()) {
                    act   = Integer.parseInt(hm.group(1));
                    scene = Integer.parseInt(hm.group(2));
                    title = hm.group(3);
                    headerFound = true;
                    continue;  // don’t include header in script text
                }
            }

            // 2) Extract music tag(s)
            boolean hasMusicOnThisLine = false;
            Matcher mm = MUSIC_PATTERN.matcher(line);
            while (mm.find()) {
                musicFile = mm.group(1).trim();
                hasMusicOnThisLine = true;
            }
            line = MUSIC_PATTERN.matcher(line).replaceAll("");

            // 3) Extract SFX tags
            Matcher sm = SFX_PATTERN.matcher(line);
            while (sm.find()) {
                sfxCues.add(sm.group(1).trim());
            }
            line = SFX_PATTERN.matcher(line).replaceAll("");

            // 4) Extract clickable terms and remove braces
            Matcher tm = TERM_PATTERN.matcher(line);
            StringBuffer sbfr = new StringBuffer();
            while (tm.find()) {
                String term = tm.group(1).trim();
                if (!clickableTerms.contains(term)) {
                clickableTerms.add(term);
                tm.appendReplacement(sbfr, term);
                System.out.println("Term: " + term + " added to clickable terms.\nCurrent Size: " + clickableTerms.size());
                }
            }
            tm.appendTail(sbfr);

            // 5) Append to cleaned script
            String textLine = sbfr.toString().trim();
            if (hasMusicOnThisLine){
                // Keep the already empty line AND one extra blank line
                cleaned.append(textLine).append("\n\n");
            }else if (!textLine.isEmpty()) {
                // Only non-empty lines get an extra line
                cleaned.append(textLine).append("\n");
            }
        }

        return new ParsedScript(
                act, scene, title,
                musicFile,
                Collections.unmodifiableSet(sfxCues),
                Collections.unmodifiableSet(clickableTerms),
                cleaned.toString()
        );
    }

    public static class ParsedScript {
        private final int act, scene;
        private final String title, musicFile, text;
        private final Set<String> sfxCues, clickableTerms;

        public ParsedScript(int act, int scene, String title,
        String musicFile, Set<String> sfxCues, Set<String> clickableTerms,
        String text) {
            this.act = act;
            this.scene = scene;
            this.title = title;
            this.musicFile = musicFile;
            this.sfxCues = sfxCues;
            this.clickableTerms = clickableTerms;
            this.text = text;
        }

//        -- Getters --
        public int getAct()              { return act; }
        public int getScene()            { return scene; }
        public String getTitle()         { return title; }
        public String getMusicFile()     { return musicFile; }
        public Set<String> getSfxCues()  { return sfxCues; }
        public Set<String> getClickableTerms() { return clickableTerms; }
        public String getText()          { return text; }
    }
}