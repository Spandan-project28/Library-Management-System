package library;

import exceptions.BookNotAvailableException;
import exceptions.InvalidStudentIdException;
import exceptions.LimitExceededException;
import model.Book;
import model.Student;

import java.time.LocalDate;
import java.util.*;

/**
 * Core Library Manager — thread-safe.
 *
 * Collections used:
 *   - ArrayList         : ordered list of all books (bookList)
 *   - HashSet           : unique student IDs (registeredStudentIds)
 *   - PriorityQueue     : overdue books sorted by earliest due date (overdueQueue)
 *   - ArrayDeque        : recent borrow/return activity log (activityLog)
 *   - Hashtable         : book ID → Book object lookup (bookTable)
 */
public class LibraryManager {

    // ------- Collections -------

    /** ArrayList: full ordered list of books for table display and search */
    private final ArrayList<Book> bookList = new ArrayList<>();

    /** HashSet: unique registered student IDs */
    private final HashSet<String> registeredStudentIds = new HashSet<>();

    /** Hashtable: fast book-ID → Book lookup */
    private final Hashtable<String, Book> bookTable = new Hashtable<>();

    /** PriorityQueue: overdue books, earliest due date = highest priority */
    private final PriorityQueue<Book> overdueQueue = new PriorityQueue<>();

    /** ArrayDeque: last-in-first-out activity log (capped at 100 entries) */
    private final ArrayDeque<String> activityLog = new ArrayDeque<>();

    /** Student registry: studentId → Student */
    private final Map<String, Student> studentRegistry = new HashMap<>();

    private static final int MAX_ACTIVITY_LOG = 100;
    private static final int BORROW_DAYS = 14;

    // =====================================================================
    //   Student Management
    // =====================================================================

    /**
     * Registers a student.
     *
     * @throws InvalidStudentIdException if the ID is null, blank, or non-alphanumeric
     */
    public synchronized void registerStudent(String studentId, String name)
            throws InvalidStudentIdException {
        validateStudentId(studentId);
        registeredStudentIds.add(studentId);
        studentRegistry.putIfAbsent(studentId, new Student(studentId, name));
        logActivity("Student registered: [" + studentId + "] " + name);
    }

    /** Returns true if the student ID is already registered. */
    public synchronized boolean isStudentRegistered(String studentId) {
        return registeredStudentIds.contains(studentId);
    }

    public synchronized Student getStudent(String studentId) {
        return studentRegistry.get(studentId);
    }

    public synchronized Set<String> getAllStudentIds() {
        return Collections.unmodifiableSet(registeredStudentIds);
    }

    // =====================================================================
    //   Book Management
    // =====================================================================

    /**
     * Adds a new book to the library.
     *
     * @throws IllegalArgumentException if bookId is already in use
     */
    public synchronized void addBook(String bookId, String title, String author, String genre)
            throws IllegalArgumentException {
        if (bookTable.containsKey(bookId)) {
            throw new IllegalArgumentException("Book ID '" + bookId + "' already exists.");
        }
        Book book = new Book(bookId, title, author, genre);
        bookList.add(book);
        bookTable.put(bookId, book);
        logActivity("Book added: [" + bookId + "] " + title);
    }

    /** Returns an unmodifiable view of the book list for table rendering. */
    public synchronized List<Book> getAllBooks() {
        return Collections.unmodifiableList(bookList);
    }

    /**
     * Searches books by title, author, genre, or ID (case-insensitive).
     */
    public synchronized List<Book> searchBooks(String query) {
        String q = query.trim().toLowerCase();
        List<Book> results = new ArrayList<>();
        for (Book b : bookList) {
            if (b.getBookId().toLowerCase().contains(q)
                    || b.getTitle().toLowerCase().contains(q)
                    || b.getAuthor().toLowerCase().contains(q)
                    || b.getGenre().toLowerCase().contains(q)) {
                results.add(b);
            }
        }
        return results;
    }

    // =====================================================================
    //   Borrow / Return
    // =====================================================================

    /**
     * Borrows a book for a student.
     *
     * @throws InvalidStudentIdException  if studentId is invalid or not registered
     * @throws BookNotAvailableException  if book doesn't exist or is already borrowed
     * @throws LimitExceededException     if student has reached their borrow limit
     */
    public synchronized void borrowBook(String studentId, String bookId)
            throws InvalidStudentIdException, BookNotAvailableException, LimitExceededException {
        try {
            validateStudentId(studentId);

            if (!registeredStudentIds.contains(studentId)) {
                throw new InvalidStudentIdException(studentId,
                        "Student ID '" + studentId + "' is not registered. Please register first.");
            }

            Book book = bookTable.get(bookId);
            if (book == null) {
                throw new BookNotAvailableException(bookId,
                        "No book found with ID '" + bookId + "'.");
            }
            if (!book.isAvailable()) {
                throw new BookNotAvailableException(bookId,
                        "Book '" + book.getTitle() + "' is already borrowed by student "
                                + book.getBorrowedByStudentId() + ".");
            }

            Student student = studentRegistry.get(studentId);
            if (!student.canBorrow()) {
                throw new LimitExceededException(studentId,
                        Student.MAX_BORROW_LIMIT, student.getBorrowedCount());
            }

            // --- Perform borrow ---
            LocalDate due = LocalDate.now().plusDays(BORROW_DAYS);
            book.setStatus(Book.Status.BORROWED);
            book.setBorrowedByStudentId(studentId);
            book.setDueDate(due);
            student.borrowBook(bookId);

            logActivity("BORROWED: [" + bookId + "] " + book.getTitle()
                    + " by student " + studentId + " | Due: " + due);

        } finally {
            // finally block — always runs (demonstrates the 'finally' keyword)
            // Could flush logs, release locks, etc.
        }
    }

    /**
     * Returns a book borrowed by a student.
     *
     * @throws InvalidStudentIdException if studentId is invalid or not registered
     * @throws BookNotAvailableException if bookId doesn't exist or wasn't borrowed by this student
     */
    public synchronized void returnBook(String studentId, String bookId)
            throws InvalidStudentIdException, BookNotAvailableException {
        try {
            validateStudentId(studentId);

            if (!registeredStudentIds.contains(studentId)) {
                throw new InvalidStudentIdException(studentId,
                        "Student ID '" + studentId + "' is not registered.");
            }

            Book book = bookTable.get(bookId);
            if (book == null) {
                throw new BookNotAvailableException(bookId,
                        "No book found with ID '" + bookId + "'.");
            }
            if (book.isAvailable()) {
                throw new BookNotAvailableException(bookId,
                        "Book '" + book.getTitle() + "' is not currently borrowed.");
            }
            if (!studentId.equals(book.getBorrowedByStudentId())) {
                throw new BookNotAvailableException(bookId,
                        "Book '" + book.getTitle() + "' was not borrowed by student " + studentId + ".");
            }

            // Check overdue
            boolean overdue = book.getDueDate() != null && LocalDate.now().isAfter(book.getDueDate());
            String overdueMsg = overdue
                    ? " [OVERDUE by " + java.time.temporal.ChronoUnit.DAYS.between(book.getDueDate(), LocalDate.now()) + " day(s)]"
                    : "";

            // --- Remove from overdue queue if present ---
            overdueQueue.remove(book);

            // --- Perform return ---
            Student student = studentRegistry.get(studentId);
            student.returnBook(bookId);
            book.setStatus(Book.Status.AVAILABLE);
            book.setBorrowedByStudentId(null);
            book.setDueDate(null);

            logActivity("RETURNED: [" + bookId + "] " + book.getTitle()
                    + " by student " + studentId + overdueMsg);

        } finally {
            // finally block — always executes after try/catch
        }
    }

    // =====================================================================
    //   Overdue Queue (used by background thread)
    // =====================================================================

    /**
     * Rebuilds the overdue PriorityQueue with books whose due date has passed.
     * Called by the background OverdueCheckerThread.
     *
     * @return snapshot list of overdue books
     */
    public synchronized List<Book> refreshOverdueQueue() {
        overdueQueue.clear();
        LocalDate today = LocalDate.now();
        for (Book b : bookList) {
            if (!b.isAvailable() && b.getDueDate() != null && today.isAfter(b.getDueDate())) {
                overdueQueue.offer(b);
            }
        }
        return new ArrayList<>(overdueQueue);
    }

    /** Peek at the most overdue book (earliest due date) */
    public synchronized Book getMostOverdueBook() {
        return overdueQueue.peek();
    }

    public synchronized int getOverdueCount() {
        return overdueQueue.size();
    }

    // =====================================================================
    //   Activity Log (ArrayDeque)
    // =====================================================================

    private void logActivity(String message) {
        String timestamped = "[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message;
        if (activityLog.size() >= MAX_ACTIVITY_LOG) {
            activityLog.pollFirst(); // remove oldest
        }
        activityLog.addLast(timestamped);
    }

    /**
     * Returns a copy of the activity log (newest first).
     */
    public synchronized List<String> getActivityLog() {
        List<String> log = new ArrayList<>(activityLog);
        Collections.reverse(log);
        return log;
    }

    // =====================================================================
    //   Validation
    // =====================================================================

    /**
     * Validates a student ID.
     *
     * @throws InvalidStudentIdException if invalid
     */
    private void validateStudentId(String studentId) throws InvalidStudentIdException {
        if (studentId == null || studentId.isBlank()) {
            throw new InvalidStudentIdException("",
                    "Student ID cannot be null or empty.");
        }
        if (!studentId.matches("[A-Za-z0-9]+")) {
            throw new InvalidStudentIdException(studentId,
                    "Student ID must be alphanumeric. Got: '" + studentId + "'.");
        }
    }

    // =====================================================================
    //   Seed Data
    // =====================================================================

    /**
     * Populates the library with sample books and students for demonstration.
     */
    public void loadSampleData() {
        String[][] books = {
                {"B001", "Clean Code",                    "Robert C. Martin", "Programming"},
                {"B002", "The Pragmatic Programmer",      "Hunt & Thomas",    "Programming"},
                {"B003", "Introduction to Algorithms",    "CLRS",             "Computer Science"},
                {"B004", "Design Patterns",               "Gang of Four",     "Software Engineering"},
                {"B005", "Java: The Complete Reference",  "Herbert Schildt",  "Java"},
                {"B006", "Effective Java",                "Joshua Bloch",     "Java"},
                {"B007", "Operating System Concepts",     "Silberschatz",     "Systems"},
                {"B008", "Computer Networks",             "Tanenbaum",        "Networking"},
                {"B009", "Database System Concepts",      "Silberschatz",     "Databases"},
                {"B010", "Artificial Intelligence",       "Russell & Norvig", "AI"},
        };

        String[][] students = {
                {"S101", "Alice Johnson"},
                {"S102", "Bob Smith"},
                {"S103", "Carol White"},
                {"S104", "David Brown"},
        };

        for (String[] b : books) {
            try { addBook(b[0], b[1], b[2], b[3]); } catch (Exception ignored) {}
        }
        for (String[] s : students) {
            try { registerStudent(s[0], s[1]); } catch (Exception ignored) {}
        }
    }
}
