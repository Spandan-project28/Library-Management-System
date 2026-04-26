import library.LibraryManager;
import ui.LibraryFrame;

import javax.swing.SwingUtilities;

/**
 * Entry point — Smart Library Issue and Return System.
 *
 * Launches the Swing UI on the Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManager library = new LibraryManager();
            library.loadSampleData();          // pre-load demo books & students

            LibraryFrame frame = new LibraryFrame(library);
            frame.setVisible(true);
        });
    }
}