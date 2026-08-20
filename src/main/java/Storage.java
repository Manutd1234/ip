import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves Wangsa tasks using a human-readable text file.
 */
public class Storage {
    private static final String FIELD_SEPARATOR = " | ";

    private final Path filePath;

    /** Creates storage that reads from and writes to the supplied path. */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads saved tasks into the supplied array.
     *
     * @param tasks destination array for the loaded tasks
     * @return the number of tasks loaded
     * @throws StorageException if the file cannot be read or contains invalid data
     */
    public int loadTasks(Task[] tasks) throws StorageException {
        if (Files.notExists(filePath)) {
            return 0;
        }

        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            if (lines.size() > tasks.length) {
                throw new StorageException("OOPS!!! The saved task list contains more than "
                        + tasks.length + " tasks.");
            }

            List<Task> loadedTasks = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                loadedTasks.add(parseTask(lines.get(i), i + 1));
            }

            for (int i = 0; i < loadedTasks.size(); i++) {
                tasks[i] = loadedTasks.get(i);
            }
            return loadedTasks.size();
        } catch (IOException exception) {
            throw new StorageException("OOPS!!! I couldn't read saved tasks from " + filePath + ".", exception);
        }
    }

    /**
     * Writes the active portion of the task array to disk.
     *
     * @param tasks tasks to save
     * @param taskCount number of active entries in the array
     * @throws StorageException if the data folder or file cannot be written
     */
    public void saveTasks(Task[] tasks, int taskCount) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            lines.add(formatTask(tasks[i]));
        }

        try {
            Path parentDirectory = filePath.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new StorageException("OOPS!!! I couldn't save tasks to " + filePath + ".", exception);
        }
    }

    /** Converts one task to a line in Wangsa's save-file format. */
    private String formatTask(Task task) {
        List<String> fields = new ArrayList<>();
        fields.add(task.getTypeIcon());
        fields.add(task.isDone() ? "1" : "0");
        fields.add(escapeField(task.getDescription()));

        if (task.getType() == TaskType.DEADLINE) {
            fields.add(escapeField(((Deadline) task).getBy()));
        } else if (task.getType() == TaskType.EVENT) {
            Event event = (Event) task;
            fields.add(escapeField(event.getFrom()));
            fields.add(escapeField(event.getTo()));
        }
        return String.join(FIELD_SEPARATOR, fields);
    }

    /** Recreates one task from a line in Wangsa's save-file format. */
    private Task parseTask(String line, int lineNumber) throws StorageException {
        List<String> fields = splitFields(line, lineNumber);
        if (fields.size() < 3) {
            throw invalidLine(lineNumber, "not enough fields");
        }

        boolean isDone;
        if (fields.get(1).equals("1")) {
            isDone = true;
        } else if (fields.get(1).equals("0")) {
            isDone = false;
        } else {
            throw invalidLine(lineNumber, "status must be 0 or 1");
        }

        String description = fields.get(2);
        if (description.isEmpty()) {
            throw invalidLine(lineNumber, "task description cannot be empty");
        }

        Task task;
        switch (fields.get(0)) {
        case "T":
            requireFieldCount(fields, 3, lineNumber);
            task = new Todo(description);
            break;
        case "D":
            requireFieldCount(fields, 4, lineNumber);
            if (fields.get(3).isEmpty()) {
                throw invalidLine(lineNumber, "deadline value cannot be empty");
            }
            task = new Deadline(description, fields.get(3));
            break;
        case "E":
            requireFieldCount(fields, 5, lineNumber);
            if (fields.get(3).isEmpty() || fields.get(4).isEmpty()) {
                throw invalidLine(lineNumber, "event start and end values cannot be empty");
            }
            task = new Event(description, fields.get(3), fields.get(4));
            break;
        default:
            throw invalidLine(lineNumber, "unknown task type");
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    /** Ensures that a saved task has exactly the fields expected for its type. */
    private void requireFieldCount(List<String> fields, int expectedCount, int lineNumber)
            throws StorageException {
        if (fields.size() != expectedCount) {
            throw invalidLine(lineNumber, "unexpected number of fields");
        }
    }

    /** Escapes separator and escape characters that occur in user-entered text. */
    private String escapeField(String field) {
        return field.replace("\\", "\\\\").replace("|", "\\|");
    }

    /** Splits a saved line while preserving escaped separators in user-entered text. */
    private List<String> splitFields(String line, int lineNumber) throws StorageException {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean isEscaped = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (isEscaped) {
                if (character != '\\' && character != '|') {
                    throw invalidLine(lineNumber, "invalid escape sequence");
                }
                currentField.append(character);
                isEscaped = false;
            } else if (character == '\\') {
                isEscaped = true;
            } else if (character == '|') {
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(character);
            }
        }

        if (isEscaped) {
            throw invalidLine(lineNumber, "unfinished escape sequence");
        }
        fields.add(currentField.toString().trim());
        return fields;
    }

    /** Builds a consistent error for a malformed save-file line. */
    private StorageException invalidLine(int lineNumber, String reason) {
        return new StorageException("OOPS!!! Saved task data is invalid at line "
                + lineNumber + ": " + reason + ".");
    }
}
