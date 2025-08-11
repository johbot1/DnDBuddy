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
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); // Use the standard arrow
        setFocusable(false); // No focus caret
        setHighlighter(null); // No text selection
    }

    public void setClickableTerms(Set<String> terms) {
        this.clickable = terms;
    }

}