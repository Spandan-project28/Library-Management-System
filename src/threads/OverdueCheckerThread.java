package threads;

import library.LibraryManager;
import model.Book;

import java.util.List;

/**
 * Background Thread — Overdue Book Checker.
 *
 * Runs every 30 seconds (configurable), checks the PriorityQueue for overdue books,
 * and notifies a listener (the UI) so it can update the overdue panel.
 *
 * Demonstrates:
 *   - Thread creation and lifecycle
 *   - Daemon threads (won't prevent JVM exit)
 *   - Synchronized access through LibraryManager
 *   - PriorityQueue usage via LibraryManager.refreshOverdueQueue()
 */
public class OverdueCheckerThread extends Thread {

    public interface OverdueListener {
        void onOverdueCheckComplete(List<Book> overdueBooks, int count);
    }

    private final LibraryManager library;
    private final OverdueListener listener;
    private volatile boolean running = true;
    private final long intervalMs;

    public OverdueCheckerThread(LibraryManager library, OverdueListener listener, long intervalMs) {
        this.library = library;
        this.listener = listener;
        this.intervalMs = intervalMs;
        setName("OverdueCheckerThread");
        setDaemon(true); // daemon — exits when main thread exits
    }

    @Override
    public void run() {
        System.out.println("[OverdueCheckerThread] Started. Checking every "
                + (intervalMs / 1000) + " seconds.");
        while (running) {
            try {
                List<Book> overdue = library.refreshOverdueQueue();
                int count = library.getOverdueCount();
                listener.onOverdueCheckComplete(overdue, count);

                Thread.sleep(intervalMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                System.out.println("[OverdueCheckerThread] Interrupted, shutting down.");
            }
        }
        System.out.println("[OverdueCheckerThread] Stopped.");
    }

    /** Gracefully stops the checker thread. */
    public void stopChecker() {
        running = false;
        interrupt();
    }
}
