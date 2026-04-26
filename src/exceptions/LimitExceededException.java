package exceptions;

/**
 * Custom Exception: Thrown when a student tries to borrow more books than allowed.
 */
public class LimitExceededException extends Exception {
    private final String studentId;
    private final int limit;
    private final int currentCount;

    public LimitExceededException(String studentId, int limit, int currentCount) {
        super("Student '" + studentId + "' has exceeded the borrow limit. "
                + "Limit: " + limit + ", Currently borrowed: " + currentCount + ".");
        this.studentId = studentId;
        this.limit = limit;
        this.currentCount = currentCount;
    }

    public String getStudentId() { return studentId; }
    public int getLimit() { return limit; }
    public int getCurrentCount() { return currentCount; }
}
