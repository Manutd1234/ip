package duke;

/**
 * A task that starts and ends at specified date or time values.
 */
public class Event extends Task {
    private final String from;
    private final String to;

    /** Creates an event with its description, start text, and end text. */
    public Event(String description, String from, String to) {
        super(description, TaskType.EVENT);
        this.from = from;
        this.to = to;
    }

    /** Returns the event's start date or time. */
    public String getFrom() {
        return from;
    }

    /** Returns the event's end date or time. */
    public String getTo() {
        return to;
    }

    @Override
    protected String getDetails() {
        return " (from: " + from + " to: " + to + ")";
    }
}
