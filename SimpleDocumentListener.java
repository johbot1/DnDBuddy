/**
 * Helper interface to make DocListeners cleaned up
 */
public interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
    void update(javax.swing.event.DocumentEvent e);

    @Override
    default void insertUpdate(javax.swing.event.DocumentEvent e) {
        update(e);
    }

    @Override
    default void removeUpdate(javax.swing.event.DocumentEvent e) {
        update(e);
    }

    @Override
    default void changedUpdate(javax.swing.event.DocumentEvent e) {
        update(e);
    }
}

