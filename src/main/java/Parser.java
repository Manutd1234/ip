import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Interprets user commands and converts their arguments into domain values.
 */
public class Parser {
    /** The supported actions that Wangsa can perform. */
    public enum CommandType {
        BYE,
        LIST,
        MARK,
        UNMARK,
        DELETE,
        ADD_TASK
    }

    /**
     * Identifies the action requested by a full command line.
     *
     * @param command full command entered by the user
     * @return the recognized command type
     * @throws WangsaException if the command is empty or unknown
     */
    public CommandType parseCommandType(String command) throws WangsaException {
        if (command.equals("bye")) {
            return CommandType.BYE;
        } else if (command.equals("list")) {
            return CommandType.LIST;
        } else if (command.equals("mark") || command.startsWith("mark ")) {
            return CommandType.MARK;
        } else if (command.equals("unmark") || command.startsWith("unmark ")) {
            return CommandType.UNMARK;
        } else if (command.equals("delete") || command.startsWith("delete ")) {
            return CommandType.DELETE;
        } else if (isTaskCommand(command)) {
            return CommandType.ADD_TASK;
        } else if (command.isEmpty()) {
            throw new WangsaException("OOPS!!! Please enter a command.");
        }
        throw new WangsaException("OOPS!!! I'm sorry, but I don't know what that means :-(");
    }

    /**
     * Creates a task from a supported task-creation command.
     *
     * @param command full todo, deadline, or event command
     * @return the task described by the command
     * @throws WangsaException if required task details are missing or invalid
     */
    public Task parseTask(String command) throws WangsaException {
        if (command.equals("todo") || command.startsWith("todo ")) {
            return parseTodo(command);
        } else if (command.equals("deadline") || command.startsWith("deadline ")) {
            return parseDeadline(command);
        } else if (command.equals("event") || command.startsWith("event ")) {
            return parseEvent(command);
        }
        throw new WangsaException("OOPS!!! I'm sorry, but I don't know what that means :-(");
    }

    /**
     * Extracts the one task number supplied to a status or delete command.
     *
     * @param command full mark, unmark, or delete command
     * @return the parsed one-based task number
     * @throws WangsaException if the argument count or number is invalid
     */
    public int parseTaskNumber(String command) throws WangsaException {
        String[] parts = command.split("\\s+");
        if (parts.length != 2) {
            throw new WangsaException("OOPS!!! " + parts[0] + " expects one task number.");
        }

        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException exception) {
            throw new WangsaException("OOPS!!! Task number must be a whole number.");
        }
    }

    /** Returns whether the command starts with a supported task keyword. */
    private boolean isTaskCommand(String command) {
        return command.equals("todo") || command.startsWith("todo ")
                || command.equals("deadline") || command.startsWith("deadline ")
                || command.equals("event") || command.startsWith("event ");
    }

    /** Creates a todo or reports its missing description. */
    private Task parseTodo(String command) throws WangsaException {
        String description = textAfterKeyword(command, "todo");
        if (description.isEmpty()) {
            throw new WangsaException("OOPS!!! The description of a todo cannot be empty.");
        }
        return new Todo(description);
    }

    /** Creates a deadline with a validated ISO date. */
    private Task parseDeadline(String command) throws WangsaException {
        String content = textAfterKeyword(command, "deadline");
        int byMarker = content.indexOf(" /by");
        if (byMarker < 0) {
            throw new WangsaException("OOPS!!! A deadline must include a description and a /by date.");
        }

        String description = content.substring(0, byMarker).trim();
        String by = content.substring(byMarker + " /by".length()).trim();
        if (description.isEmpty()) {
            throw new WangsaException("OOPS!!! The description of a deadline cannot be empty.");
        }
        if (by.isEmpty()) {
            throw new WangsaException("OOPS!!! A deadline needs a value after /by.");
        }

        try {
            return new Deadline(description, LocalDate.parse(by));
        } catch (DateTimeParseException exception) {
            throw new WangsaException("OOPS!!! Deadline date must be valid and use yyyy-MM-dd format "
                    + "(e.g., 2019-10-15).");
        }
    }

    /** Creates an event or reports its missing description, start, or end. */
    private Task parseEvent(String command) throws WangsaException {
        String content = textAfterKeyword(command, "event");
        int fromMarker = content.indexOf(" /from");
        int toMarker = fromMarker < 0 ? -1 : content.indexOf(" /to", fromMarker + " /from".length());
        if (fromMarker < 0 || toMarker < 0) {
            throw new WangsaException("OOPS!!! An event must include a description, /from start, and /to end.");
        }

        String description = content.substring(0, fromMarker).trim();
        String from = content.substring(fromMarker + " /from".length(), toMarker).trim();
        String to = content.substring(toMarker + " /to".length()).trim();
        if (description.isEmpty()) {
            throw new WangsaException("OOPS!!! The description of an event cannot be empty.");
        }
        if (from.isEmpty() || to.isEmpty()) {
            throw new WangsaException("OOPS!!! An event needs values after /from and /to.");
        }
        return new Event(description, from, to);
    }

    /** Returns the trimmed text after a command keyword. */
    private String textAfterKeyword(String command, String keyword) {
        return command.length() == keyword.length()
                ? ""
                : command.substring(keyword.length()).trim();
    }
}
