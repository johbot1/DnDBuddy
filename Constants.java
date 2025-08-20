import java.awt.*;

public final class Constants {
    // Do not use the constructor. There is nothing to use
    private Constants() {};

    // Vars
    // -- Main Menu --
    public static final int FOUNDATION_PANEL_WIDTH = 500;
    public static final int FOUNDATION_PANEL_HEIGHT = 400;
    public static final int INSET_VALS = 20;
    public static final int TITLE_TEXT_SIZE = 32;
    public static final int BUTTON_TEXT_SIZE = 26;
    public static final int BUTTON_PREFERRED_WIDTH = 150;
    public static final int BUTTON_PREFERRED_HEIGHT = 50;
    public static final int GRIDBAG_WIDTH = 2;
    public static final int GRIDBAG_GRID_SPACING = 1;
    public static final double GRIDBAG_GRID_WEIGHT = 0.5;
    public static final Color GRADIENT_COLOR_1 = new Color(81, 84, 94);
    public static final Color GRADIENT_COLOR_2 = new Color(33, 24, 77);
    public static final Color BUTTON_BG_COLOR = new Color(70, 70, 70, 200);
    public static final Color BUTTON_HOVER_COLOR = new Color(100, 100, 100, 220);

    // -- Groove Buddy GUI --
    public static final int GB_CONTROLPANEL_GAP_SPACING = 10;
    public static final double GB_SPLITPANE_RESIZE = 0.8;
    public static final int GB_FOUNDATIONPANEL_WIDTH = 1000;
    public static final int GB_FOUNDATIONPANEL_HEIGHT = 600;
    public static final int GB_FILEBROWSER_SPACING = 5;
    public static final Dimension GB_FILEBROWSER_DIMENSION = new Dimension(200, 0);
    public static final Dimension GB_BUTTON_SIZE = new Dimension(80, 30);
//    public static final int
//    public static final int

    public static int scaledToFit(int dimension1, int dimension2){
        return (dimension1 - dimension2)/2;
    }
}
