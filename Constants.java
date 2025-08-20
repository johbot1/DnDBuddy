import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class Constants {
    // Do not use the constructor. There is nothing to use
    private Constants() {};

    // Vars
    // -- Main Menu --
    public static final int MM_FOUNDATION_PANEL_WIDTH = 500;
    public static final int MM_FOUNDATION_PANEL_HEIGHT = 400;
    public static final int MM_INSET_VALS = 20;
    public static final int MM_TITLE_TEXT_SIZE = 32;
    public static final int MM_BUTTON_TEXT_SIZE = 26;
    public static final int MM_BUTTON_PREFERRED_WIDTH = 150;
    public static final int MM_BUTTON_PREFERRED_HEIGHT = 50;
    public static final int MM_GRIDBAG_WIDTH = 2;
    public static final int MM_GRIDBAG_GRID_SPACING = 1;
    public static final double MM_GRIDBAG_GRID_WEIGHT = 0.5;
    public static final Color MM_GRADIENT_COLOR_1 = new Color(81, 84, 94);
    public static final Color MM_GRADIENT_COLOR_2 = new Color(33, 24, 77);
    public static final Color MM_BUTTON_BG_COLOR = new Color(70, 70, 70, 200);
    public static final Color MM_BUTTON_HOVER_COLOR = new Color(100, 100, 100, 220);

    // -- Groove Buddy GUI --
    public static final int GB_HGAP_SPACING = 10;
    public static final int GB_FOUNDATIONPANEL_WIDTH = 1000;
    public static final int GB_FOUNDATIONPANEL_HEIGHT = 600;
    public static final int GB_VGAP_SPACING = 5;
    public static final int GB_INSET_VALS = 5;
    public static final double GB_SPLITPANE_RESIZE = 0.8;
    public static final Dimension GB_FILEBROWSER_DIMENSION = new Dimension(200, 0);
    public static final Dimension GB_BUTTON_SIZE = new Dimension(80, 30);
    public static final EmptyBorder GB_CONTROLCONTAINER_BORDER = new EmptyBorder(10, 10, 10, 10);
//    public static final int
//    public static final int

    public static int MM_scaledToFit(int dimension1, int dimension2){
        return (dimension1 - dimension2)/2;
    }
}
