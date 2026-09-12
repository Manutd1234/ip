package duke;

/**
 * Associates a search result with its task number in the full list.
 *
 * @param taskNumber one-based position used by mark, unmark, and delete
 * @param task matching task
 */
public record TaskMatch(int taskNumber, Task task) {
}
