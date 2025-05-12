import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.event.MouseEvent;
import java.util.Set;

// A JTextPane that knows how to show tooltips for clickable terms
class LinkTextPane extends JTextPane {
    private Set<String> clickables = Set.of();

    public LinkTextPane() {
        super();
        // enable Swing tooltips
        setToolTipText("");
        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setInitialDelay(100);   // show quickly
        ToolTipManager.sharedInstance().setDismissDelay(10_000); // linger 10s
    }

    public void setClickableTerms(Set<String> terms) {
        this.clickables = terms;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int pos = viewToModel2D(e.getPoint());
        if (pos < 0) return null;
        try {
            int start = Utilities.getWordStart(this, pos);
            int end   = Utilities.getWordEnd  (this, pos);
            String word = getDocument().getText(start, end - start);
            if (clickables.contains(word)) {
                return "Open sheet for \"" + word + "\"";
            }
        } catch (BadLocationException ignored) {}
        return null;
    }
}