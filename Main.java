import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Ensure GUI is built on the EDT
        SwingUtilities.invokeLater(() -> {

            // Phase 1.1: Window Creation
            JFrame frame = new JFrame("Arcanum Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null); // center on screen

            // Phase 1.2: Basic Window Layout
            frame.setLayout(new BorderLayout());

            // 1.2.1 Script Viewer placeholder (read-only)
            JTextPane scriptViewer = new JTextPane();
            scriptViewer.setEditable(false);
            scriptViewer.setText("Adventure script will appear here…");
            scriptViewer.setMargin(new Insets(10,10,10,10));
            JScrollPane scriptScroll = new JScrollPane(scriptViewer);
            frame.add(scriptScroll, BorderLayout.CENTER);

            // 1.2.2 Controls placeholder on the right
            JPanel controlsPane = new JPanel();
            controlsPane.setPreferredSize(new Dimension(250, 0));
            controlsPane.setLayout(null); // for absolute placement, may not need?
            frame.add(controlsPane, BorderLayout.EAST);

            frame.setVisible(true);
        });
    }
}