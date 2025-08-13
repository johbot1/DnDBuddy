import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the DnD Buddy Application
 * <p>
 * Simple menu to launch the different modules
 */
public class Main {
    /**
     * The main method that starts the application
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Start all Swing apps on the Event Dispatch Thread
        SwingUtilities.invokeLater(() ->{
            try{
                //Use system for styling and native design
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                createMainMenu();
            }catch (Exception e){
                e.printStackTrace();
                //Show an Error Dialogue if the menu can't be created
                JOptionPane.showMessageDialog(null,
                        "A critical error occured on startup",
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Creates/Draws the main menu
     */
    private static void createMainMenu() {
        // 1. Create the window
        JFrame frmFoundation = new JFrame("DnD Buddy - Main Menu");
        frmFoundation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmFoundation.setSize(400,250);
        frmFoundation.setLocationRelativeTo(null);

        // 2. Create a panel for buttons
        JPanel pnlFoundationPanel = new JPanel(new GridLayout(2,1,15,15)); // 2 row, 1 column with gaps
        pnlFoundationPanel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30)); // Padding

        // 3. Create the Buttons
        // 3.1 Groove Buddy Button
        JButton btnGrooveBuddy = new JButton("Groove Buddy");
        btnGrooveBuddy.setFont(new Font("Arial", Font.BOLD,16));
        btnGrooveBuddy.addActionListener(e ->{
            // THIS is what creates + shows the window for Groove Buddy
            MusicLooperGUI grooveBuddy = new MusicLooperGUI();
            grooveBuddy.initUI();
        });

        // 3.2 Quit button
        JButton btnQuit = new JButton("Quit");
        btnQuit.setFont(new Font("Arial", Font.BOLD, 16));
        btnQuit.addActionListener(e -> {
            // This safely exits the entire application.
            System.exit(0);
        });

        // 4. Add the buttons to the panel
        pnlFoundationPanel.add(btnGrooveBuddy);
        pnlFoundationPanel.add(btnQuit);

        // 5. Add the panel to the frame to make it visible
        frmFoundation.add(pnlFoundationPanel);
        frmFoundation.setVisible(true);
    }

}
