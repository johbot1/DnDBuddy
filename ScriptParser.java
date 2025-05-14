import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String act = "", scene = "", title = "";
        String currentMusicFile = null;

        Set<String> clickableTerms = new LinkedHashSet<>();
        Set<String> sfxCues       = new LinkedHashSet<>();
        Set<String> musicCues     = new LinkedHashSet<>();
        StringBuilder cleaned     = new StringBuilder();

        // Begin Parsing
        boolean headerFound = false;
        for (String raw : lines) {
            String line = raw;

            // 1) Header extraction
            if (act.isEmpty() && line.startsWith("ACT ")) {
                act = line;
                continue;
            }
            if (scene.isEmpty() && line.startsWith("SCENE ")) {
                scene = line;
                continue;
            }
            if (title.isEmpty() && line.startsWith("TITLE:")) {
                title = line.substring("TITLE:".length()).trim();
                continue;
            }

            // 2) Extract MUSIC tags and placeholders
            boolean hasMusicOnThisLine = false;
            Matcher mm = MUSIC_PATTERN.matcher(line);
            while (mm.find()) {
                currentMusicFile = mm.group(1).trim();
                hasMusicOnThisLine = true;
                // Build a display name and record it
                String display = extractDisplayName(currentMusicFile);
                musicCues.add(display);

                // Replace tag with bracketed placeholder
                line = line.replaceFirst(Pattern.quote(mm.group(0)), "[" + display + "]");
            }

            // 3) Extract SFX tags
            Matcher sm = SFX_PATTERN.matcher(line);
            StringBuffer sbSfx = new StringBuffer();
            while (sm.find()) {
                sfxCues.add(sm.group(1).trim());
                sm.appendReplacement(sbSfx, "");
            }
            sm.appendTail(sbSfx);
            line = sbSfx.toString();

            // 4) Extract clickable terms and remove ALL braces
            Matcher tm = TERM_PATTERN.matcher(line);
            StringBuffer sbClick = new StringBuffer();
            while (tm.find()) {
                String term = tm.group(1).trim();
                clickableTerms.add(term);          // add to set (duplicates ignored)
                tm.appendReplacement(sbClick, term);  // always strip the `{}`
                System.out.println("Term: " + term + " added to clickable terms.\nCurrent Size: " + clickableTerms.size());

            }
            tm.appendTail(sbClick);

            // 5) Collapse blank lines, but keep one after MUSIC
            String textLine = sbClick.toString().trim();
            if (hasMusicOnThisLine) {
                cleaned.append(textLine).append("\n\n");
            } else if (!textLine.isEmpty()) {
                cleaned.append(textLine).append("\n");
            }
        }

        return new ParsedScript(
                act,
                scene,
                title,
                currentMusicFile,
                Collections.unmodifiableSet(sfxCues),
                Collections.unmodifiableSet(clickableTerms),
                Collections.unmodifiableSet(sfxCues),
                cleaned.toString()
        );
    }



    /**
     * Converts a filename into a human-readable display name:
     * Strips extensions |
     * Splits camelCase |
     * Replaces underscores/hyphens with spaces
     */
    private static String extractDisplayName(String filename) {
        String name = filename;
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            name = name.substring(0, dotIndex);
        }
        // split camelCase boundaries
        name = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        // replace underscores/hyphens
        name = name.replaceAll("[-_]", " ");
        return name.trim();
    }
}
