/**
 * A task without an associated date or time.
 */
public class Todo extends Task {
    /** Creates a todo task with the given description. */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String getTypeIcon() {
        return "T";
    }
}
