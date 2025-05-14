import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Set;

// A JTextPane that knows how to show tooltips for clickable terms
class LinkTextPane extends JTextPane {
    private Set<String> clickable = Set.of();

    public LinkTextPane() {
        super();
        // enable Swing tooltips
        setToolTipText(" ");    // give it a single space (or some placeholder)
        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setInitialDelay(100);   // show quickly
        ToolTipManager.sharedInstance().setDismissDelay(1000); // linger 1s
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); // Use the standard arrow
        setFocusable(false); // No focus caret
        setHighlighter(null); // No text selection
    }

    public void setClickableTerms(Set<String> terms) {
        this.clickable = terms;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        String defaultTip = super.getToolTipText(e);
        int pos = viewToModel2D(e.getPoint());
        if (pos < 0) return defaultTip;
        try {
        int start = Utilities.getWordStart(this, pos);
        int end   = Utilities.getWordEnd  (this, pos);
        String word = getDocument().getText(start, end - start);
        if (clickable.contains(word)) {
            return "Open sheet for \"" + word + "\"";
        }
    } catch (BadLocationException ignored) {}
//  TEMPORARY: always show this placeholder so we know tooltips are wired up
        return " ";  // a non-empty string (here a non-breaking space)
    }
}