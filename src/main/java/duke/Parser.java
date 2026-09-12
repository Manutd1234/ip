package duke;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interprets user commands and converts their arguments into domain values.
 */
public class Parser {
    private static final Pattern DETAIL_MARKER = Pattern.compile("(?<!\\S)/(by|from|to)(?=\\s|$)");

    /** Creates a parser for Wangsa command lines. */
    public Parser() {
    }

    /** The supported actions that Wangsa can perform. */
    public enum CommandType {
        /** Exit the application. */ BYE,
        /** Display all tasks. */ LIST,
        /** Mark a task complete. */ MARK,
        /** Mark a task incomplete. */ UNMARK,
        /** Delete a task. */ DELETE,
        /** Search task descriptions. */ FIND,
        /** Sort tasks by their deadlines. */ SORT,
        /** Add a task. */ ADD_TASK
    }

    /**
     * Parses and validates a complete command into a typed command object.
     *
     * @param command complete command entered by the user
     * @return validated command ready for the application service
     * @throws WangsaException if the command or one of its arguments is invalid
     */
    public Command parse(String command) throws WangsaException {
        command = command.trim();
        CommandType commandType = parseCommandType(command);
        return switch (commandType) {
        case BYE, LIST, SORT -> new Command.Simple(commandType);
        case MARK, UNMARK, DELETE -> new Command.TaskNumber(commandType, parseTaskNumber(command));
        case FIND -> new Command.Search(parseSearchKeyword(command));
        case ADD_TASK -> new Command.AddTask(parseTask(command));
        };
    }

    /**
     * Identifies the action requested by a full command line.
     *
     * @param command full command entered by the user
     * @return the recognized command type
     * @throws WangsaException if the command is empty or unknown
     */
    public CommandType parseCommandType(String command) throws WangsaException {
        command = command.trim();
        if (command.isEmpty()) {
            throw new WangsaException("OOPS!!! Please enter a command.");
        }

        String keyword = firstWord(command);
        return switch (keyword) {
        case "bye" -> requireExactCommand(command, keyword, CommandType.BYE);
        case "list" -> requireExactCommand(command, keyword, CommandType.LIST);
        case "mark" -> CommandType.MARK;
        case "unmark" -> CommandType.UNMARK;
        case "delete" -> CommandType.DELETE;
        case "find" -> CommandType.FIND;
        case "sort" -> requireExactCommand(command, keyword, CommandType.SORT);
        case "todo", "deadline", "event" -> CommandType.ADD_TASK;
        default -> throw unknownCommand();
        };
    }

    /**
     * Creates a task from a supported task-creation command.
     *
     * @param command full todo, deadline, or event command
     * @return the task described by the command
     * @throws WangsaException if required task details are missing or invalid
     */
    public Task parseTask(String command) throws WangsaException {
        command = command.trim();
        return switch (firstWord(command)) {
        case "todo" -> parseTodo(command);
        case "deadline" -> parseDeadline(command);
        case "event" -> parseEvent(command);
        default -> throw unknownCommand();
        };
    }

    /**
     * Extracts the one task number supplied to a status or delete command.
     *
     * @param command full mark, unmark, or delete command
     * @return the parsed one-based task number
     * @throws WangsaException if the argument count or number is invalid
     */
    public int parseTaskNumber(String command) throws WangsaException {
        command = command.trim();
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

    /**
     * Extracts the keyword used to search task descriptions.
     *
     * @param command full find command.
     * @return non-empty search keyword.
     * @throws WangsaException if the keyword is missing
     */
    public String parseSearchKeyword(String command) throws WangsaException {
        command = command.trim();
        String keyword = textAfterKeyword(command, "find");
        if (keyword.isEmpty()) {
            throw new WangsaException("OOPS!!! Find needs a keyword to search for.");
        }
        return keyword;
    }

    /** Returns the first whitespace-delimited word in a command. */
    private String firstWord(String command) {
        int keywordEnd = 0;
        while (keywordEnd < command.length() && !Character.isWhitespace(command.charAt(keywordEnd))) {
            keywordEnd++;
        }
        return command.substring(0, keywordEnd);
    }

    /** Returns a command type only when a command that takes no arguments is exact. */
    private CommandType requireExactCommand(String command, String keyword, CommandType commandType)
            throws WangsaException {
        if (!command.equals(keyword)) {
            throw unknownCommand();
        }
        return commandType;
    }

    /** Creates the standard error for an unrecognized command. */
    private WangsaException unknownCommand() {
        return new WangsaException("OOPS!!! I'm sorry, but I don't know what that means :-(");
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
        List<String> fields = splitDetails(content,
                "A deadline must include a description and a /by date.", "by");
        String description = fields.get(0);
        String by = fields.get(1);
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
        List<String> fields = splitDetails(content,
                "An event must include a description, /from start, and /to end.", "from", "to");
        String description = fields.get(0);
        String from = fields.get(1);
        String to = fields.get(2);
        if (description.isEmpty()) {
            throw new WangsaException("OOPS!!! The description of an event cannot be empty.");
        }
        if (from.isEmpty() || to.isEmpty()) {
            throw new WangsaException("OOPS!!! An event needs values after /from and /to.");
        }
        return new Event(description, from, to);
    }

    /** Splits standalone detail markers, requiring each marker once in the expected order. */
    private List<String> splitDetails(String content, String formatHint, String... expectedMarkers)
            throws WangsaException {
        Matcher matcher = DETAIL_MARKER.matcher(content);
        List<String> fields = new ArrayList<>();
        int fieldStart = 0;
        while (matcher.find()) {
            int markerIndex = fields.size();
            if (markerIndex >= expectedMarkers.length
                    || !matcher.group(1).equals(expectedMarkers[markerIndex])) {
                throw new WangsaException("OOPS!!! Use each detail marker once and in order. " + formatHint);
            }
            fields.add(content.substring(fieldStart, matcher.start()).trim());
            fieldStart = matcher.end();
        }
        if (fields.size() != expectedMarkers.length) {
            throw new WangsaException("OOPS!!! " + formatHint);
        }
        fields.add(content.substring(fieldStart).trim());
        return fields;
    }

    /** Returns the trimmed text after a command keyword. */
    private String textAfterKeyword(String command, String keyword) {
        return command.length() == keyword.length()
                ? ""
                : command.substring(keyword.length()).trim();
    }
}
