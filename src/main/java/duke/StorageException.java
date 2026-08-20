package duke;

/**
 * Reports a problem reading or writing Wangsa's saved task data.
 */
public class StorageException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a storage error with a user-facing explanation.
     * @param message explanation for the failure
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Creates a storage error with its underlying input/output cause.
     * @param message explanation for the failure
     * @param cause underlying failure
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
