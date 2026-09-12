package duke;

/**
 * Provides the command reference shown by {@code help} and used in AI answers.
 *
 * <p>Keeping the reference here lets both interfaces offer help without creating
 * an AI client. Update this text whenever a command's behavior changes.</p>
 */
public final class CommandHelp {
    private static final String REFERENCE = """
            Wangsa commands:
            todo DESCRIPTION - add a task without a date.
            deadline DESCRIPTION /by YYYY-MM-DD - add a task due on a valid date.
            event DESCRIPTION /from START /to END - add an event; start and end are free-form text.
            list - show all tasks and their numbers.
            find KEYWORD - find descriptions containing a phrase, ignoring case; keep full-list numbers.
            sort - put deadlines first, earliest to latest, then undated tasks; save the new order.
            mark NUMBER - complete a task using its current list number.
            unmark NUMBER - reopen a completed task.
            delete NUMBER - immediately remove a task; there is no undo.
            help - show this command reference without AI or internet access.
            @ai QUESTION - ask about Wangsa commands; replies do not execute commands.
            bye - exit Wangsa.

            Use lowercase commands and markers. Task numbers start at 1; sorting or deleting can change them.
            You can keep up to 100 tasks, including completed ones. Your changes are saved automatically.
            Priorities, reminders, recurring tasks, and editing task descriptions are not supported.
            """.strip();

    private CommandHelp() {
    }

    /**
     * Returns the built-in command reference without needing a key or internet connection.
     *
     * @return The command reference as plain text.
     */
    public static String getText() {
        return REFERENCE;
    }
}
