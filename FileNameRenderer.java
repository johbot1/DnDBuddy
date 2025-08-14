import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * A custom display for file names in the browser, and not the full path
 */
public class FileNameRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
        if (value instanceof File file) {
            setText(file.getName()); // Only show the name
        }
        return this;
    }
}