# Project Report: Smart Library Issue and Return System

## 2. Problem Understanding
- **What the system does:** The system is a graphical Java application that manages the core operations of a library. It allows registering students, adding books, searching the catalogue, borrowing, returning, tracking overdue books, and viewing system activity.
- **The main objective:** To develop a robust, thread-safe library management system utilizing OOP principles, Java Collections Framework, Multithreading, and Exception Handling, wrapped in an interactive Swing GUI.
- **The users of the system:** Library Administrators/Librarians who manage the books, and students who interact with the system to borrow/return books.

## 3. Concepts Applied
- **Exception Handling:** Used `try`, `catch`, `multiple catch`, and `finally` blocks to gracefully handle runtime errors during borrowing, returning, and multithreading simulations without crashing the app.
- **Custom Exceptions:** Created `BookNotAvailableException`, `InvalidStudentIdException`, and `LimitExceededException` (using `throw` and `throws`) to represent specific business logic failures.
- **Multithreading:** Employed threads to simulate concurrent borrowing (multiple students accessing the library simultaneously) and a background daemon thread (`OverdueCheckerThread`) to continuously monitor and update overdue books.
- **Synchronisation:** Applied the `synchronized` keyword to critical methods in the core manager to prevent race conditions when multiple threads read/write to the book and student data simultaneously.
- **Collections Framework:** 
  - `ArrayList`: Maintains the main ordered list of books for rendering the catalogue.
  - `HashSet`: Ensures unique student IDs.
  - `PriorityQueue`: Organizes overdue books, automatically prioritizing those with the oldest due dates.
  - `ArrayDeque`: Implements a fast LIFO (Last-In-First-Out) activity log.
  - `Hashtable`: Provides fast, thread-safe lookups mapping Book IDs to Book objects.
- **Swing GUI:** Built an interactive desktop application using components like `JFrame`, `JPanel`, `JTable`, `JComboBox`, `JTextArea`, and `JOptionPane` to provide a user-friendly experience.
- **MVC Architecture:** Separated the application into Model (`Book`, `Student`), View (`LibraryFrame`, `BookTablePanel`), and Controller/Manager (`LibraryManager`, Threads) to keep code modular and maintainable.

## 4. System Design
### Class Names & How They Are Used
- `Main`: Application entry point. Loads sample data and launches the GUI.
- `LibraryManager`: Core logic controller managing all collections and synchronized operations.
- `Book` & `Student`: Data models representing entities in the system.
- `LibraryFrame`: Main Swing GUI containing the tabs and forms.
- `BookTablePanel`: Custom UI component rendering the catalogue table.
- `OverdueCheckerThread`: Background thread scanning for overdue books.
- `ConcurrentBorrowSimulator`: Simulates multithreaded borrowing access.

### Important Methods
- `borrowBook(...)` & `returnBook(...)` in `LibraryManager`: Handle transaction logic, throw custom exceptions, and apply `synchronized`.
- `refreshOverdueQueue()`: Iterates through books to rebuild the `PriorityQueue` for overdue items.

### Relationship Among Classes
- `LibraryManager` manages collections of `Book` and `Student`.
- `LibraryFrame` interacts with `LibraryManager` to display data and execute user commands (Controller-View relationship).
- Threads (`OverdueCheckerThread`, `ConcurrentBorrowSimulator`) hold references to `LibraryManager` to invoke synchronized methods safely.

### Diagrams
**Class Diagram**
```text
[LibraryFrame] ---> [LibraryManager]
[OverdueCheckerThread] ---> [LibraryManager]
[ConcurrentBorrowSimulator] ---> [LibraryManager]

[LibraryManager] o--> (1..*) [Book]
[LibraryManager] o--> (1..*) [Student]
```

**Use Case Diagram**
```text
Actor: Librarian / User
 --> (Add Book)
 --> (Register Student)
 --> (Borrow Book)
 --> (Return Book)
 --> (Simulate Concurrency)

Actor: Background System Thread
 --> (Check Overdue Books)
```

## 5. Swing UI Design Explanation
- **Components Used:** `JFrame` for the main window, `JTabbedPane` for navigation, `JTable` for the catalogue, `JTextField` for text inputs, `JComboBox` for genre selection, `JButton` for actions, `JTextArea` for the activity log, and `JOptionPane` for error dialogs.
- **Containers Used:** `JPanel` serves as the primary container to group elements inside each tab. `JScrollPane` is used to allow scrolling for the `JTable` and `JTextArea`.
- **Layout Manager Used:** 
  - `BorderLayout` for main structural panels (Header, Center, Footer).
  - `GridBagLayout` for form alignment in the "Add Book" and "Register Student" tabs to ensure neat label-input pairs.
  - `FlowLayout` for button groupings and search bars.
- **Event-handling logic:** `ActionListener`s were attached to buttons using lambda expressions (`e -> doAction()`) to trigger backend operations in `LibraryManager` and refresh the UI upon completion.
- **Why Swing was used:** Swing provides a rich, cross-platform set of lightweight desktop components that are built directly into standard Java, eliminating the need for external UI dependencies while allowing highly customizable interfaces.

## 6. Implementation Code

Below is the complete Java code for the most critical components of the system showcasing Exceptions, Collections, Multithreading, and UI Logic. 
*(Note: Minor UI styling classes like `UITheme.java` and `BookTablePanel.java` are omitted for brevity in the report but are present in the project `src` folder).*

### Custom Exceptions
```java
package exceptions;
public class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String bookId, String message) { super(message); }
}

public class InvalidStudentIdException extends Exception {
    public InvalidStudentIdException(String studentId, String message) { super(message); }
}

public class LimitExceededException extends Exception {
    public LimitExceededException(String studentId, int limit, int currentCount) {
        super("Student '" + studentId + "' has exceeded the borrow limit. Limit: " + limit + ", Currently borrowed: " + currentCount + ".");
    }
}
```

### Multithreading Simulation
```java
package threads;
import library.LibraryManager;
import exceptions.*;
import java.util.List;
import java.util.Random;

public class ConcurrentBorrowSimulator implements Runnable {
    private final LibraryManager library;
    private final List<String> studentIds;
    private final List<String> bookIds;

    public ConcurrentBorrowSimulator(LibraryManager library, List<String> studentIds, List<String> bookIds) {
        this.library = library;
        this.studentIds = studentIds;
        this.bookIds = bookIds;
    }

    @Override
    public void run() {
        Thread[] threads = new Thread[studentIds.size()];
        Random random = new Random();

        for (int i = 0; i < studentIds.size(); i++) {
            final String studentId = studentIds.get(i);
            final String bookId = bookIds.get(random.nextInt(bookIds.size()));

            threads[i] = new Thread(() -> {
                try {
                    Thread.sleep(random.nextInt(300));
                    library.borrowBook(studentId, bookId); // Synchronised method
                } catch (BookNotAvailableException | LimitExceededException | InvalidStudentIdException e) {
                    System.out.println("Exception Caught: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.out.println("Thread execution finished for: " + studentId);
                }
            });
        }
        for (Thread t : threads) t.start();
    }
}
```

### Core Logic (Collections & Synchronization)
```java
package library;
import exceptions.*;
import model.Book;
import model.Student;
import java.time.LocalDate;
import java.util.*;

public class LibraryManager {
    // Required Collections Framework
    private final ArrayList<Book> bookList = new ArrayList<>();
    private final HashSet<String> registeredStudentIds = new HashSet<>();
    private final Hashtable<String, Book> bookTable = new Hashtable<>();
    private final PriorityQueue<Book> overdueQueue = new PriorityQueue<>();
    private final ArrayDeque<String> activityLog = new ArrayDeque<>();
    private final Map<String, Student> studentRegistry = new HashMap<>();

    public synchronized void borrowBook(String studentId, String bookId) 
            throws InvalidStudentIdException, BookNotAvailableException, LimitExceededException {
        try {
            if (!registeredStudentIds.contains(studentId)) {
                throw new InvalidStudentIdException(studentId, "Student ID not registered.");
            }
            Book book = bookTable.get(bookId);
            if (book == null || !book.isAvailable()) {
                throw new BookNotAvailableException(bookId, "Book not available.");
            }
            Student student = studentRegistry.get(studentId);
            if (!student.canBorrow()) {
                throw new LimitExceededException(studentId, 3, student.getBorrowedCount());
            }

            book.setStatus(Book.Status.BORROWED);
            book.setBorrowedByStudentId(studentId);
            student.borrowBook(bookId);
            activityLog.addLast("BORROWED: " + bookId + " by " + studentId);
        } finally {
            // Finally block guarantees execution
        }
    }
}
```

## 7. Output Screenshots
*(Please insert screenshots of your application here before submission)*
- [ ] Main GUI (Catalogue Tab)
- [ ] Successful operation (e.g., Book Borrowed dialog)
- [ ] Error/exception message (e.g., Invalid Student ID dialog)
- [ ] Table or output display (Activity Log Tab)

## 8. Test Cases

| Test Case | Input | Expected Output | Actual Output |
| :--- | :--- | :--- | :--- |
| **Valid entry** | Register Student: "S105", "John" | Success message, student registered | Success message, student registered |
| **Invalid input** | Borrow Book: Student ID="", Book ID="B001" | `InvalidStudentIdException` caught, error dialog | Error dialog shown: "Student ID cannot be empty" |
| **Duplicate record** | Add Book with existing ID "B001" | Exception caught, error dialog | Error dialog shown: "Book ID already exists" |
| **Thread test** | Click "Run Simulation" button | Concurrent threads output log of success/fails | Threads execute concurrently, log updates safely |
| **GUI button test**| Click "Search" with empty input | Catalogue resets to show all books | Catalogue displays all books correctly |

## 9. Challenges Faced
- **Thread Synchronisation:** Initially, allowing multiple students to borrow the same book simultaneously could lead to race conditions where a single book is assigned to multiple students. *Solution:* Used the `synchronized` keyword in the `borrowBook` and `returnBook` methods of `LibraryManager` to ensure thread-safety.
- **Overdue Queue Management:** Continually checking due dates in the main UI thread caused freezing. *Solution:* Offloaded this task to a background daemon thread (`OverdueCheckerThread`) that safely interacts with the `PriorityQueue`.
- **UI Responsiveness:** Modifying Swing components from background threads threw exceptions. *Solution:* Used `SwingUtilities.invokeLater()` inside the thread listeners to safely update the GUI.

## 10. Conclusion
This assignment provided practical, hands-on experience in building a complete, professional Java application. I learned how to seamlessly integrate advanced Java features like Multithreading, custom Exception Handling, and the Collections Framework with a graphical user interface using Swing. It highlighted the importance of thread-safety in concurrent environments and the value of using the right data structures (like `PriorityQueue` for overdue items and `Hashtable` for fast lookups) to optimize system performance and reliability.
