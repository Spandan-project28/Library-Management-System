package threads;

import exceptions.BookNotAvailableException;
import exceptions.InvalidStudentIdException;
import exceptions.LimitExceededException;
import library.LibraryManager;

import java.util.List;
import java.util.Random;

/**
 * Simulates multiple students trying to borrow books concurrently.
 *
 * Demonstrates:
 *   - Runnable interface
 *   - Multiple threads accessing shared resources
 *   - Synchronized access enforced by LibraryManager
 *   - try / catch / multiple-catch / finally inside a thread
 */
public class ConcurrentBorrowSimulator implements Runnable {

    public interface SimulationListener {
        void onSimulationEvent(String message, boolean isError);
        void onSimulationComplete();
    }

    private final LibraryManager library;
    private final SimulationListener listener;
    private final List<String> studentIds;
    private final List<String> bookIds;

    private static final Random random = new Random();

    public ConcurrentBorrowSimulator(
            LibraryManager library,
            SimulationListener listener,
            List<String> studentIds,
            List<String> bookIds) {
        this.library = library;
        this.listener = listener;
        this.studentIds = studentIds;
        this.bookIds = bookIds;
    }

    @Override
    public void run() {
        Thread[] threads = new Thread[studentIds.size()];

        for (int i = 0; i < studentIds.size(); i++) {
            final String studentId = studentIds.get(i);
            final String bookId = bookIds.get(random.nextInt(bookIds.size()));

            threads[i] = new Thread(() -> {
                try {
                    // Small random delay to simulate real-world timing
                    Thread.sleep(random.nextInt(300));

                    library.borrowBook(studentId, bookId);

                    listener.onSimulationEvent(
                            "✔ " + studentId + " successfully borrowed [" + bookId + "]", false);

                } catch (BookNotAvailableException e) {
                    listener.onSimulationEvent(
                            "✘ " + studentId + " failed: " + e.getMessage(), true);

                } catch (LimitExceededException e) {
                    listener.onSimulationEvent(
                            "✘ " + studentId + " limit exceeded: " + e.getMessage(), true);

                } catch (InvalidStudentIdException e) {
                    listener.onSimulationEvent(
                            "✘ Invalid student " + studentId + ": " + e.getMessage(), true);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    listener.onSimulationEvent("Thread interrupted for " + studentId, true);

                } finally {
                    // finally: always runs — good place to update UI counters or release resources
                }
            }, "BorrowThread-" + studentId);
        }

        // Start all threads at roughly the same time to demonstrate concurrency
        for (Thread t : threads) t.start();

        // Wait for all threads to complete
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        listener.onSimulationComplete();
    }

    /** Launches the simulation in a new background thread. */
    public Thread startSimulation() {
        Thread simThread = new Thread(this, "SimulationCoordinator");
        simThread.setDaemon(true);
        simThread.start();
        return simThread;
    }
}
