package exceptions;

/**
 * Custom Exception: Thrown when a requested book is not available for borrowing.
 */
public class BookNotAvailableException extends Exception {
    private final String bookId;

    public BookNotAvailableException(String bookId) {
        super("Book with ID '" + bookId + "' is not available for borrowing.");
        this.bookId = bookId;
    }

    public BookNotAvailableException(String bookId, String message) {
        super(message);
        this.bookId = bookId;
    }

    public String getBookId() {
        return bookId;
    }
}
