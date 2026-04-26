package exceptions;

/**
 * Custom Exception: Thrown when a student ID is invalid (null, empty, wrong format).
 */
public class InvalidStudentIdException extends Exception {
    private final String studentId;

    public InvalidStudentIdException(String studentId) {
        super("Invalid Student ID: '" + studentId + "'. ID must be non-empty and alphanumeric.");
        this.studentId = studentId;
    }

    public InvalidStudentIdException(String studentId, String message) {
        super(message);
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }
}
