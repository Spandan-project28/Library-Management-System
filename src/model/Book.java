package model;

import java.time.LocalDate;

/**
 * Represents a Book in the library system.
 * Implements Comparable for PriorityQueue usage (by due date for overdue priority).
 */
public class Book implements Comparable<Book> {

    public enum Status { AVAILABLE, BORROWED }

    private final String bookId;
    private String title;
    private String author;
    private String genre;
    private Status status;
    private String borrowedByStudentId;  // null if available
    private LocalDate dueDate;           // null if available

    public Book(String bookId, String title, String author, String genre) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.status = Status.AVAILABLE;
        this.borrowedByStudentId = null;
        this.dueDate = null;
    }

    // ---- Getters ----

    public String getBookId()              { return bookId; }
    public String getTitle()               { return title; }
    public String getAuthor()              { return author; }
    public String getGenre()               { return genre; }
    public Status getStatus()              { return status; }
    public String getBorrowedByStudentId() { return borrowedByStudentId; }
    public LocalDate getDueDate()          { return dueDate; }

    // ---- Setters ----

    public void setTitle(String title)     { this.title = title; }
    public void setAuthor(String author)   { this.author = author; }
    public void setGenre(String genre)     { this.genre = genre; }

    public void setStatus(Status status)               { this.status = status; }
    public void setBorrowedByStudentId(String id)      { this.borrowedByStudentId = id; }
    public void setDueDate(LocalDate dueDate)          { this.dueDate = dueDate; }

    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    /**
     * Comparator for PriorityQueue: books with the earliest due date have highest priority (most overdue).
     */
    @Override
    public int compareTo(Book other) {
        if (this.dueDate == null && other.dueDate == null) return 0;
        if (this.dueDate == null) return 1;
        if (other.dueDate == null) return -1;
        return this.dueDate.compareTo(other.dueDate);
    }

    /**
     * Returns a string array for use in JTable rows:
     * [ID, Title, Author, Genre, Status, BorrowedBy, DueDate]
     */
    public String[] toTableRow() {
        return new String[]{
                bookId,
                title,
                author,
                genre,
                status.name(),
                borrowedByStudentId != null ? borrowedByStudentId : "-",
                dueDate != null ? dueDate.toString() : "-"
        };
    }

    @Override
    public String toString() {
        return "[" + bookId + "] " + title + " by " + author
                + " | " + genre + " | " + status.name();
    }
}
