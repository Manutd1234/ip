/**
 * A task entered into the Wangsa task list.
 */
public class Task {
    /** The text entered by the user for this task. */
    protected String description;

    /** Whether the task has been marked as done. */
    protected boolean isDone;

    /**
     * Creates an unfinished task with the given description.
     *
     * @param description the task text
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /** Marks this task as done. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this task as not done. */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns the one-character status marker used in task output.
     *
     * @return {@code X} for a completed task, otherwise a blank space
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Returns the task in the format shown to the user.
     *
     * @return the status marker and task description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
