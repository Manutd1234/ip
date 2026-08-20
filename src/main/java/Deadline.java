/**
 * A task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    private final String by;

    /** Creates a deadline with its description and deadline text. */
    public Deadline(String description, String by) {
        super(description, TaskType.DEADLINE);
        this.by = by;
    }

    /** Returns the date or time by which this task should be completed. */
    public String getBy() {
        return by;
    }

    @Override
    protected String getDetails() {
        return " (by: " + by + ")";
    }
}
