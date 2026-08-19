/**
 * The supported kinds of tasks in Wangsa.
 */
public enum TaskType {
    TODO("T"),
    DEADLINE("D"),
    EVENT("E");

    private final String icon;

    TaskType(String icon) {
        this.icon = icon;
    }

    /** Returns the one-letter marker used when displaying this task type. */
    public String getIcon() {
        return icon;
    }
}
