package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Student in the library system.
 */
public class Student {

    public static final int MAX_BORROW_LIMIT = 3;

    private final String studentId;
    private String name;

    /** Books currently borrowed by this student (book IDs). */
    private final List<String> borrowedBookIds;

    public Student(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
        this.borrowedBookIds = new ArrayList<>();
    }

    public String getStudentId()          { return studentId; }
    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }
    public List<String> getBorrowedBookIds() { return borrowedBookIds; }

    public int getBorrowedCount() {
        return borrowedBookIds.size();
    }

    public boolean canBorrow() {
        return borrowedBookIds.size() < MAX_BORROW_LIMIT;
    }

    public void borrowBook(String bookId) {
        borrowedBookIds.add(bookId);
    }

    public void returnBook(String bookId) {
        borrowedBookIds.remove(bookId);
    }

    @Override
    public String toString() {
        return "[" + studentId + "] " + name + " (Borrowed: " + borrowedBookIds.size() + "/" + MAX_BORROW_LIMIT + ")";
    }
}
