import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

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
        // Configure macOS-specific properties BEFORE creating any GUI components
        configureMacOSProperties();

        // Start all Swing apps on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                //Use system for styling and native design
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                createMainMenu();
            } catch (Exception e) {
                //Show an Error Dialogue if the menu can't be created
                JOptionPane.showMessageDialog(null,
                        "A critical error occurred on startup",
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Sets up macOS-specific system properties for proper dock behavior
     */
    private static void configureMacOSProperties() {
        System.setProperty("apple.awt.application.name", "DnD Buddy");
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DnD Buddy");
        System.setProperty("apple.awt.application.bundleid", "com.dndbuddy.app");
    }

    /**
     * Creates/Draws the main menu with background styling
     */
    private static void createMainMenu() {
        // 1. Create the window
        JFrame frmFoundation = new JFrame("DnD Buddy - Main Menu");
        setupApplicationIcon(frmFoundation);

        frmFoundation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmFoundation.setSize(500, 400); // Made slightly larger to accommodate background
        frmFoundation.setLocationRelativeTo(null);
        frmFoundation.setResizable(false); // Prevents background scaling issues

        // 2. Create the background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());

        // 3. Create the main content panel with buttons
        JPanel contentPanel = createStyledContentPanel();

        // 4. Add content to background panel
        backgroundPanel.add(contentPanel, BorderLayout.CENTER);

        // 5. Add background panel to frame
        frmFoundation.setContentPane(backgroundPanel);
        frmFoundation.setVisible(true);
    }

    /**
     * Creates the styled content panel with buttons
     */
    private static JPanel createStyledContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setOpaque(false); // Make transparent to show background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;

        // Title label (optional - adds nice styling)
        JLabel titleLabel = new JLabel("DnD Buddy", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE); // Adjust color based on your background
//        titleLabel.setShadowEffect(); // Custom method we'll add
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);

        // Create styled buttons
        JButton btnGrooveBuddy = createStyledButton("Groove Buddy");
        btnGrooveBuddy.addActionListener(e -> {
            MusicLooperGUI grooveBuddy = new MusicLooperGUI();
            grooveBuddy.initUI();
        });

        JButton btnQuit = createStyledButton("Quit");
        btnQuit.addActionListener(e -> System.exit(0));

        // Add buttons with spacing
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        contentPanel.add(btnGrooveBuddy, gbc);

        gbc.gridx = 1;
        contentPanel.add(btnQuit, gbc);

        return contentPanel;
    }

    /**
     * Creates a styled button with consistent appearance
     */
    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Serif", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 50));

        // Style the button for better visibility over background
        button.setBackground(new Color(70, 70, 70, 200)); // Semi-transparent dark
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(100, 100, 100, 220));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(70, 70, 70, 200));
            }
        });

        return button;
    }

    /**
     * Sets up the application icon for both window and dock
     */
    private static void setupApplicationIcon(JFrame frame) {
        try {
            URL iconURL = Main.class.getResource("resources/images/icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image image = icon.getImage();
                frame.setIconImage(image);

                if (Taskbar.isTaskbarSupported()) {
                    Taskbar taskbar = Taskbar.getTaskbar();
                    if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        taskbar.setIconImage(image);
                    }
                    if (taskbar.isSupported(Taskbar.Feature.ICON_BADGE_TEXT)) {
                        taskbar.setIconBadge("DnD");
                    }
                }
            } else {
                System.err.println("Couldn't find icon file at: resources/images/icon.png");
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
        }
    }

    /**
     * Custom JPanel that paints a background image
     */
    private static class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;
        private static final String BACKGROUND_PATH = "resources/images/mm_background.jpg"; // or .png

        public BackgroundPanel() {
            loadBackgroundImage();
        }

        private void loadBackgroundImage() {
            try {
                URL backgroundURL = Main.class.getResource(BACKGROUND_PATH);
                if (backgroundURL != null) {
                    backgroundImage = ImageIO.read(backgroundURL);
                } else {
                    System.err.println("Background image not found at: " + BACKGROUND_PATH);
                    // Create a fallback gradient background
                    createGradientBackground();
                }
            } catch (Exception e) {
                System.err.println("Error loading background image: " + e.getMessage());
                createGradientBackground();
            }
        }

        private void createGradientBackground() {
            // Create a simple gradient as fallback
            backgroundImage = new BufferedImage(500, 400, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = backgroundImage.createGraphics();

            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(81, 84, 94),     // Light Blue Top #CFD5EF
                    0, 400, new Color(33, 24, 77)    // Darker Purple Bottom #543DC4
            );

            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, 500, 400);
            g2d.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (backgroundImage != null) {
                Graphics2D g2d = (Graphics2D) g.create();

                // Enable anti-aliasing for smoother scaling
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Scale and draw the background image
                int panelWidth = getWidth();
                int panelHeight = getHeight();

                // Scale to Fit
                double scaleX = (double) panelWidth / backgroundImage.getWidth();
                double scaleY = (double) panelHeight / backgroundImage.getHeight();
                double scale = Math.max(scaleX, scaleY); // Fill entire panel

                int scaledWidth = (int) (backgroundImage.getWidth() * scale);
                int scaledHeight = (int) (backgroundImage.getHeight() * scale);
                int x = (panelWidth - scaledWidth) / 2;
                int y = (panelHeight - scaledHeight) / 2;

                g2d.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, null);

                g2d.dispose();
            }
        }
    }
}